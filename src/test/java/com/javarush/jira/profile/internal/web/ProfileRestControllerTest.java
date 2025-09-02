package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

@WebMvcTest(ProfileRestController.class)
public class ProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AbstractProfileController abstractProfileController; // Мокаем базовый класс

    private static final String REST_URL = "/api/profile";

    // Пример JSON для обновления
    private final String profileJson = "{\"name\":\"John\",\"email\":\"john@example.com\"}";

    // --- Тест для метода get ---

    @Test
    public void get_ShouldReturnProfile_WhenSuccess() throws Exception {
        // Создаем контакты
        Set<ContactTo> contacts = Set.of(
                new ContactTo("EMAIL", "john@example.com"),
                new ContactTo("PHONE", "+123456789")
        );

        // Создаем профиль
        ProfileTo profile = new ProfileTo(1L, Set.of("NEWS", "OFFERS"), contacts);

        mockMvc.perform(get(REST_URL)
                        .principal(() -> "1")) // или используйте @WithMockUser
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John")); // пример проверки
    }

    @Test
    public void get_ShouldReturnError_WhenException() throws Exception {
        when(abstractProfileController.get(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get(REST_URL)
                        .principal(() -> "1"))
                .andExpect(status().isInternalServerError()); // или другой статус
    }

    // --- Тест для метода update ---

    @Test
    public void update_ShouldReturnNoContent_WhenSuccess() throws Exception {
        doNothing().when(abstractProfileController).update(any(ProfileTo.class), anyLong());

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson)
                        .principal(() -> "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void update_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        String invalidJson = "{\"name\":\"\"}"; // пример, нарушающий @Valid

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .principal(() -> "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_ShouldReturnError_WhenException() throws Exception {
        doThrow(new RuntimeException("Error")).when(abstractProfileController).update(any(ProfileTo.class), anyLong());

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson)
                        .principal(() -> "1"))
                .andExpect(status().isInternalServerError());
    }
}