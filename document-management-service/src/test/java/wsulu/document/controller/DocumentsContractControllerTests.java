package wsulu.document.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wsulu.document.service.DocumentZipService;
import wsulu.document.service.DocumentsContractService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("unused")
class DocumentsContractControllerTests {

    private MockMvc mockMvc;

    @Mock
    private DocumentsContractService documentsContractService;

    @Mock
    private DocumentZipService documentZipService;

    @InjectMocks
    private DocumentsContractController documentsContractController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(documentsContractController).build();
    }

    @Test
    void generateShouldReturnTrueWhenServiceReturnsTrue() throws Exception {
        String request = "{\"data\":[{\"param1\":\"value1\",\"TEMPLATE\":\"testTemplate\"}]}";
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    void getDocumentShouldReturnPdfWhenParametersAreValid() throws Exception {
        mockMvc.perform(get("/document")
                        .param("loanId", "1")
                        .param("clientId", "1")
                        .param("templateName", "testTemplate"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=testTemplate.pdf"));
    }

    @Test
    void individualContractPreviewShouldReturnPdfWhenInputIsValid() throws Exception {
        String request = "{\"data\":[{\"param1\":\"value1\",\"TEMPLATE\":\"testTemplate\"}]}";
        mockMvc.perform(post("/individualContract/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    void getAllZipDocumentByLoanShouldReturnZipWhenParametersAreValid() throws Exception {
        mockMvc.perform(get("/document/all")
                        .param("loanId", "1")
                        .param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=all_docs.zip"));
    }

    @Test
    void getAllZipDocumentLastVersionByLoanShouldReturnZipWhenParametersAreValid() throws Exception {
        mockMvc.perform(get("/document/last")
                        .param("loanId", "1")
                        .param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=all_docs_last_version.zip"));
    }

    @Test
    void getDocumentShouldReturnBadRequestWhenParametersAreMissing() throws Exception {
        mockMvc.perform(get("/document"))
                .andExpect(status().isBadRequest());
    }
}