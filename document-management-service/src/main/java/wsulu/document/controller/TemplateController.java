package wsulu.document.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import wsulu.document.service.DocumentsContractService;
import wsulu.document.service.TemplateService;

@Tag(name = "Template controller", description = "The controller is responsible for saving document templates in .docx format in the database and loading them from the database." +
        "Templates are saved with the possibility of versioning")
@RestController
@RequestMapping("/template")
@SuppressWarnings("unused")
public class TemplateController {

    private final TemplateService templateService;
    private final DocumentsContractService documentsContractService;

    @Autowired
    public TemplateController(TemplateService templateService, DocumentsContractService documentsContractService) {
        this.templateService = templateService;
        this.documentsContractService = documentsContractService;
    }

    @Operation(summary = "Method of saving a document template in the database", description = "The method saves the document template as a byte array in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "true - the document template was successfully saved in the database" +
                    "false - document template saving failed")})
    @PostMapping(value = "/saveTemplate")
    public boolean uploadTemplateByRest(@Parameter(description = "Document template file in docs format", required = true) @RequestParam("file") MultipartFile file,
                                        @Parameter(description = "Title of document", required = true) @RequestParam("title") String templateName,
                                        Authentication auth) {
        return templateService.saveTemplateDocx(file, templateName, auth.getName());
    }

    @Operation(summary = "Returns the document template", description = "The method is responsible for obtaining (unloading) a document template from the database as a byte array in .docx format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the document template in .docx format as a byte array")})
    @GetMapping(
            value = "/template",
            produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @ResponseBody
    public byte[] getTemplate(@Parameter(description = "document template identifier in database", required = true) @RequestParam("templateId") Long templateId) {
        return templateService.getTemplateById(templateId);
    }

    @Operation(summary = "Returns the latest version of the document template", description = "The method is responsible for obtaining (unloading) the document template from the database as a byte array in .docx format," +
            "unloads the latest version of the template by name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the document template in .docx format as a byte array")})
    @GetMapping(
            value = "/templateTitle",
            produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @ResponseBody
    public byte[] getTemplate(@Parameter(description = "document template name", required = true) @RequestParam("templateTitle") String templateName,
                              HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=" + templateName + ".docx");
        return templateService.getLastTemplateBodyByTitle(templateName);
    }
}
