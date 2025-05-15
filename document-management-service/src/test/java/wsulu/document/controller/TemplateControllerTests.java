package wsulu.document.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wsulu.document.service.DocumentsContractService;
import wsulu.document.service.TemplateService;

import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class TemplateControllerTests {

    @Mock
    private TemplateService templateService;

    @Mock
    private DocumentsContractService documentsContractService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TemplateController templateController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(templateController).build();
        lenient().when(authentication.getName()).thenReturn("testUser");
    }

    @Test
    void uploadTemplateByRestShouldReturnTrueWhenSaveSuccessful() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test content".getBytes()
        );
        when(templateService.saveTemplateDocx(any(), anyString(), anyString())).thenReturn(true);
        mockMvc.perform(multipart("/template/saveTemplate")
                        .file(file)
                        .param("title", "Test Template")
                        .principal(authentication)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(templateService).saveTemplateDocx(any(), eq("Test Template"), eq("testUser"));
    }

    @Test
    void uploadTemplateByRestShouldReturnFalseWhenSaveFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test content".getBytes()
        );
        when(templateService.saveTemplateDocx(any(), anyString(), anyString())).thenReturn(false);
        mockMvc.perform(multipart("/template/saveTemplate")
                        .file(file)
                        .param("title", "Test Template")
                        .principal(authentication)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getTemplateShouldReturnTemplateWhenTemplateExists() throws Exception {
        byte[] templateContent = "template content".getBytes();
        when(templateService.getTemplateById(1L)).thenReturn(templateContent);
        final String DOCX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        mockMvc.perform(get("/template/template")
                        .param("templateId", "1")
                        .accept(DOCX_MEDIA_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().bytes(templateContent))
                .andExpect(header().string("Content-Type", DOCX_MEDIA_TYPE));
        verify(templateService).getTemplateById(1L);
    }

    @Test
    void getTemplateByTitle_ShouldSetContentDispositionHeader() throws Exception {
        byte[] templateContent = "template content".getBytes();
        String templateName = "Test Template";
        when(templateService.getLastTemplateBodyByTitle(anyString())).thenReturn(templateContent);
        mockMvc.perform(get("/template/templateTitle")
                        .param("templateTitle", templateName))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=" + templateName + ".docx"));
    }
}