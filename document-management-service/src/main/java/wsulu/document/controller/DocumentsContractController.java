package wsulu.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wsulu.document.service.DocumentZipService;
import wsulu.document.service.DocumentsContractService;

import java.util.List;
import java.util.Map;

@Tag(name = "Document controller", description = "The controller is responsible for handling customer documents in .pdf format")
@RestController
@SuppressWarnings("unused")
public class DocumentsContractController {

    private final DocumentsContractService documentsContractService;
    private final DocumentZipService documentZipService;

    @Autowired
    public DocumentsContractController(
            DocumentsContractService documentsContractService,
            DocumentZipService documentZipService) {
        this.documentsContractService = documentsContractService;
        this.documentZipService = documentZipService;
    }

    @Operation(summary = "Method for generating a pdf document", description = "The method generates a pdf document based on incoming parameters and a template." +
            "Allows you to generate multiple documents at the same time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "true - the document was successfully generated and saved in the database" +
                    "false - document creation failed")})
    @PostMapping(value = "/generate")
    public boolean generate(@RequestBody(description = "List of maps, where key is the name of the parameter in the template," +
            "value is the parameter. The list always contains a map with the key TEMPLATE in the value stores " +
            "the name of the template that will be selected to generate the document", required = true)
                            List<Map<String, Object>> data) {
        return documentsContractService.generateAndSave(data);
    }

    @Operation(summary = "Method for retrieving a document", description = "Outputs a previously created document stored in the database")
    @GetMapping(value = "/document", produces = "application/pdf")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the document as an array of bytes")})
    public byte[] getDocument(@Parameter(description = "Identifier of the loan for which data is requested", required = true) @RequestParam Long loanId,
                              @Parameter(description = "Identifier of the client for whom the data is requested", required = true) @RequestParam Long clientId,
                              @Parameter(description = "Document template name", required = true) @RequestParam String templateName,
                              HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=" + templateName + ".pdf");
        return documentsContractService.getDocumentsByLoan(
                loanId, clientId, templateName);
    }

    @Operation(summary = "Method returns a preview of the document", description = "Returns a preliminary document that has not been saved in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the document as an array of bytes")})
    @PostMapping(value = "/individualContract/preview", produces = "application/pdf")
    public byte[] individualContractPreview(@RequestBody(description = "The map, where key is the name of the parameter in the template, value is the parameter", required = true) Map<String, Object> data) {
        return documentsContractService.previewDocumentsContract(data);
    }

    @Operation(summary = "Method of obtaining all documents", description = "Returns all client documents for a particular loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a zip archive with all client documents as a byte array")})
    @GetMapping(value = "/document/all", produces = "application/zip")
    public byte[] getAllZipDocumentByLoan(@Parameter(description = "Identifier of the loan for which data is requested", required = true) @RequestParam Long loanId,
                                          @Parameter(description = "Identifier of the client for whom the data is requested", required = true) @RequestParam Long clientId,
                                          HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=all_docs.zip");
        return documentZipService.getAllDocumentByLoanId(loanId, clientId, true);
    }

    @Operation(summary = "Method of obtaining all client documents of the latest version", description = "Returns all client documents of the latest version for a particular loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a zip archive with all client documents of the latest version as a byte array")})
    @GetMapping(value = "/document/last", produces = "application/zip")
    public byte[] getAllZipDocumentLastVersionByLoan(@Parameter(description = "Identifier of the loan for which data is requested", required = true) @RequestParam Long loanId,
                                                     @Parameter(description = "Identifier of the client for whom the data is requested", required = true) @RequestParam Long clientId,
                                                     HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=all_docs_last_version.zip");
        return documentZipService.getAllDocumentByLoanId(loanId, clientId, false);
    }
}
