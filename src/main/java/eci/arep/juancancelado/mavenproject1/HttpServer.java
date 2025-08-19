package eci.arep.juancancelado.mavenproject1;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import com.google.gson.Gson; // Usa Gson (añádelo a tu pom.xml si usas Maven)

public class HttpServer {

    private static final List<Map<String, String>> tasks = new ArrayList<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en puerto " + port);

        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                handleClient(clientSocket);
            }
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String inputLine = in.readLine();
        if (inputLine == null || inputLine.isEmpty()) {
            return;
        }

        System.out.println("Solicitud: " + inputLine);
        String[] requestParts = inputLine.split(" ");
        String method = requestParts[0];
        String path = requestParts[1];

        while ((in.readLine()) != null && !in.ready()) {
        }

        if (path.startsWith("/tasks")) {
            handleTasks(out, method, path);
        } else {
            serveStaticFile(out, path);
        }

        in.close();
        out.close();
    }

    private static void handleTasks(OutputStream out, String method, String path) throws IOException {
        Map<String, String> params = getParams(path);
        String name = params.getOrDefault("name", "Anon");
        String type = params.getOrDefault("type", "otro"); // ahora usamos "type"

        switch (method.toUpperCase()) {
            case "GET":
                sendResponse(out, "application/json", gson.toJson(tasks));
                break;

            case "POST":
                if (!name.equals("Anon")) {
                    Map<String, String> task = new HashMap<>();
                    task.put("name", name);
                    task.put("type", type); // guardamos tipo
                    tasks.add(task);
                    sendResponse(out, "text/plain", "Tarea agregada: " + name + " (" + type + ")");
                } else {
                    sendResponse(out, "text/plain", "Tarea inválida.");
                }
                break;

            case "DELETE":
                boolean removed = tasks.removeIf(t
                        -> t.get("name").equals(name) && t.get("type").equals(type)
                );
                if (removed) {
                    sendResponse(out, "text/plain", "Tarea eliminada: " + name + " (" + type + ")");
                } else {
                    sendResponse(out, "text/plain", "Tarea no encontrada: " + name);
                }
                break;
                
            case "RESET":
                tasks.clear();
                sendResponse(out, "text/plain", "Lista reiniciada");
                break;

            default:
                sendResponse(out, "text/plain", "Método no soportado: " + method);
        }
    }

    private static Map<String, String> getParams(String path) {
        Map<String, String> params = new HashMap<>();
        if (path.contains("?")) {
            String query = path.split("\\?")[1];
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    params.put(kv[0], URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        }
        return params;
    }

    private static void serveStaticFile(OutputStream out, String path) throws IOException {
        if (path.equals("/")) {
            path = "/index.html";
        }
        File file = new File("public" + path);
        if (file.exists() && !file.isDirectory()) {
            String contentType = guessContentType(file.getName());
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            sendBinaryResponse(out, contentType, fileBytes);
        } else {
            sendResponse(out, "text/plain", "404 Not Found");
        }
    }

    private static String guessContentType(String fileName) {
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpg", "image/jpeg");
        return mimeTypes.getOrDefault(fileName.substring(fileName.lastIndexOf(".") + 1), "application/octet-stream");
    }

    private static void sendResponse(OutputStream out, String contentType, String content) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + content.getBytes().length + "\r\n"
                + "\r\n"
                + content;
        out.write(response.getBytes());
    }

    private static void sendBinaryResponse(OutputStream out, String contentType, byte[] content) throws IOException {
        String headers = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + content.length + "\r\n"
                + "\r\n";
        out.write(headers.getBytes());
        out.write(content);
    }
}
