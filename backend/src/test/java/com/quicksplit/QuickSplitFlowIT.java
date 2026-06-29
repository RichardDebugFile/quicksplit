package com.quicksplit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueba de integracion del recorrido completo de la aplicacion sobre la base H2 en memoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
class QuickSplitFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode register(String name, String email, String password) throws Exception {
        String body = """
                {"name":"%s","email":"%s","password":"%s"}
                """.formatted(name, email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(JsonNode auth) {
        return "Bearer " + auth.get("token").asText();
    }

    @Test
    @DisplayName("Recorrido completo: registro, grupo, miembro, gasto y plan de pagos")
    void fullExpenseSharingFlow() throws Exception {
        JsonNode ana = register("Ana", "ana@quicksplit.com", "password123");
        JsonNode beto = register("Beto", "beto@quicksplit.com", "password123");
        String anaToken = bearer(ana);
        long anaId = ana.get("user").get("id").asLong();
        long betoId = beto.get("user").get("id").asLong();

        // Ana crea un grupo.
        MvcResult groupResult = mockMvc.perform(post("/api/groups")
                        .header(HttpHeaders.AUTHORIZATION, anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Viaje\",\"description\":\"Fin de semana\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andReturn();
        long groupId = objectMapper.readTree(groupResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Ana agrega a Beto.
        mockMvc.perform(post("/api/groups/" + groupId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"beto@quicksplit.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2));

        // Ana registra un gasto de 30 dividido en partes iguales entre ambos.
        String expenseBody = """
                {"description":"Cena","amount":30.00,"paidByUserId":%d,
                 "splitType":"EQUAL","participantUserIds":[%d,%d]}
                """.formatted(anaId, anaId, betoId);
        mockMvc.perform(post("/api/groups/" + groupId + "/expenses")
                        .header(HttpHeaders.AUTHORIZATION, anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shares.length()").value(2));

        // El plan de pagos: Beto le paga 15.00 a Ana.
        MvcResult settlement = mockMvc.perform(get("/api/groups/" + groupId + "/settlement")
                        .header(HttpHeaders.AUTHORIZATION, anaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].from.id").value(betoId))
                .andExpect(jsonPath("$.transactions[0].to.id").value(anaId))
                .andReturn();

        JsonNode tx = objectMapper.readTree(settlement.getResponse().getContentAsString())
                .get("transactions").get(0);
        assertThat(tx.get("amount").asDouble()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("Sin token, los endpoints protegidos rechazan la peticion")
    void protectedEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("No se puede registrar dos veces el mismo email")
    void duplicateEmailIsRejected() throws Exception {
        register("Cris", "cris@quicksplit.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cris2\",\"email\":\"cris@quicksplit.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Login con credenciales incorrectas devuelve 401")
    void loginWithBadCredentials() throws Exception {
        register("Dani", "dani@quicksplit.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dani@quicksplit.com\",\"password\":\"wrongpass1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El registro valida los campos de entrada")
    void registrationValidatesInput() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"no-es-email\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
