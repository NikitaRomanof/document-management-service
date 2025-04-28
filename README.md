# Document Generation and Management System

## Project Description

The project is a system for working with document templates and generating PDF files based on them. Key features:

- Storage of document templates in DOCX format with versioning support
- PDF document generation from templates with data substitution
- Client document management (storage, retrieval, archiving)
- Support for multiple document versions per client

## Core Functionality

### Template Management
- Uploading new versions of document templates to the system
- Retrieving templates by ID or name (always returns the latest version)
- Maintaining version history for each template

### Document Generation
- Creating PDF documents from templates and input data
- Batch generation of multiple documents simultaneously
- Document preview before saving

### Client Document Management
- Saving generated documents linked to clients and loans
- Retrieving individual documents or all documents for a client
- Creating ZIP archives with documents (all versions or only latest)

## Technologies

- **Java**
- **Spring Boot**
- **Spring Data JPA**
- **Swagger (OpenAPI 3.0)** for API documentation
- DOCX/PDF generation

## API Endpoints

### Documents Controller (`DocumentsContractController`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/generate` | Generate one or multiple documents |
| GET    | `/document` | Retrieve specific document as PDF |
| POST   | `/individualContract/preview` | Preview document before saving |
| GET    | `/document/all` | Get all client documents (ZIP) |
| GET    | `/document/last` | Get latest versions of all client documents (ZIP) |

### Templates Controller (`TemplateController`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/template/saveTemplate` | Save new document template |
| GET    | `/template/template` | Get template by ID |
| GET    | `/template/templateTitle` | Get latest template version by name |

## Installation and Setup

1. Clone the repository
2. Configure database connection in `application.properties`
3. Build the project: `mvn clean install`
4. Run: `java -jar target/DocumentManagementApplication.jar`
