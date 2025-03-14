@RestController
@RequestMapping("/api/files")
public class FileController {

    private final String uploadDir = "/path/to/upload/directory/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Path path = Paths.get(uploadDir + file.getOriginalFilename());
            Files.write(path, file.getBytes());
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
        }
    }
}


@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    try {
        Path path = Paths.get(uploadDir + filename);
        Resource resource = new UrlResource(path.toUri());
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    } catch (MalformedURLException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
