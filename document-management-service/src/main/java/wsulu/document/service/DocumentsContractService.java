package wsulu.document.service;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public interface DocumentsContractService {

    /**
     * Retrieve a saved document from the database by loan id and document name
     * @param loanId loan id in the database
     * @param clientId client id in the database
     * @param templateName document name
     * @return document as a byte array in .pdf format
     */
    byte[] getDocumentsByLoan(@NotNull Long loanId,
                              @NotNull Long clientId,
                              @NotNull String templateName);

    /**
     * Generation of a preliminary document without saving it to the database.
     * @param data map with variables, where the key is the name of the variable and the value is the variable itself. Always contains the key - TEMPLATE
     * @return byte array containing the generated .pdf document
     */
    byte[] previewDocumentsContract(@NotNull Map<String, Object> data);

    /**
     * Generation of any number of client documents with subsequent saving to the database as a byte array
     * @param data list of map,where the key is the name of the variable and the value is the variable itself. Always contains the key - TEMPLATE
     * @return boolean result of the attempt to generate and save the document to the database
     */
    boolean generateAndSave(@NotNull List<Map<String, Object>> data);
}
