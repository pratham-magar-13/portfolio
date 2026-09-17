package com.jackdaw.portfolio;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jackdaw.portfolio.repository.ContactMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContactMessageRepository repository;

    @Test
    void homePageLoads() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pratham Thapa Magar")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void apiDocsExposePortfolioEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Portfolio API")))
                .andExpect(content().string(containsString("/contact")));
    }

    @Test
    void validContactSubmissionIsSaved() throws Exception {
        long before = repository.count();
        mvc.perform(
                        post("/contact")
                                .header("X-Forwarded-For", "10.0.0.11")
                                .param("name", "Test User")
                                .param("email", "test@example.com")
                                .param("message", "Hello from a test"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/?sent=true#contact"));
        assertEquals(before + 1, repository.count());
    }

    @Test
    void honeypotSubmissionIsIgnored() throws Exception {
        long before = repository.count();
        mvc.perform(
                        post("/contact")
                                .header("X-Forwarded-For", "10.0.0.12")
                                .param("name", "Bot")
                                .param("email", "bot@example.com")
                                .param("message", "spam")
                                .param("website", "http://spam.example"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/?sent=true#contact"));
        assertEquals(before, repository.count());
    }

    @Test
    void contactIsRateLimited() throws Exception {
        String ip = "10.0.0.13";
        for (int i = 0; i < 5; i++) {
            mvc.perform(
                            post("/contact")
                                    .header("X-Forwarded-For", ip)
                                    .param("name", "Frequent User")
                                    .param("email", "frequent@example.com")
                                    .param("message", "message " + i))
                    .andExpect(status().isFound());
        }
        mvc.perform(
                        post("/contact")
                                .header("X-Forwarded-For", ip)
                                .param("name", "Frequent User")
                                .param("email", "frequent@example.com")
                                .param("message", "one too many"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/?limited=true#contact"));
    }

    @Test
    void invalidContactShowsError() throws Exception {
        mvc.perform(
                        post("/contact")
                                .header("X-Forwarded-For", "10.0.0.14")
                                .param("name", "")
                                .param("email", "not-an-email")
                                .param("message", "hi"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/#contact"));
    }
}
