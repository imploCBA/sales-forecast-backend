@RestController
@RequestMapping("/api")
public class ForecastController {

    @PostMapping("/forecast")
    public ResponseEntity<String> runForecast(@RequestBody ForecastRequest request) {
        try {
            // Путь к скрипту
            String scriptPath = "src/main/resources/scripts/forecast.py";

            // Параметры для скрипта
            String startDate = request.getStartDate();
            String endDate = request.getEndDate();
            String quarters = String.valueOf(request.getQuarters());

            // Команда для выполнения
            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, startDate, endDate, quarters);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Чтение вывода скрипта
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ResponseEntity.ok(output.toString());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при выполнении скрипта.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Исключение: " + e.getMessage());
        }
    }
}
