package eci.arep.juancancelado.mavenproject1;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        int port = 35000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en puerto " + port);

        while (true) { // Aceptar múltiples solicitudes seguidas
            try (Socket clientSocket = serverSocket.accept()) {
                handleClient(clientSocket);
            }
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        // Leer primera línea (request line)
        String inputLine = in.readLine();
        if (inputLine == null || inputLine.isEmpty()) return;

        System.out.println("Solicitud: " + inputLine);
        String[] requestParts = inputLine.split(" ");
        String method = requestParts[0];
        String path = requestParts[1];

        // Leer resto de encabezados (hasta línea vacía)
        while (in.ready()) {
            String headerLine = in.readLine();
            if (headerLine.isEmpty()) break;
        }

        // Rutas REST
        if (path.startsWith("/hello")) {
    String name = getNameParam(path);
    sendResponse(out, "text/plain", "Hola " + name + " desde GET");
}
else if (path.startsWith("/hellopost")) {
    String name = getNameParam(path);
    sendResponse(out, "text/plain", "Hola " + name + " desde POST");
}
else {
    serveStaticFile(out, path);
}


        in.close();
        out.close();
    }
    private static String getNameParam(String path) {
    if (path.contains("?name=")) {
        return path.split("\\?name=")[1];
    }
    return "Anon";
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
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("ico", "image/x-icon");

        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return mimeTypes.getOrDefault(ext, "application/octet-stream");
    }

    private static void sendResponse(OutputStream out, String contentType, String content) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.getBytes().length + "\r\n" +
                "\r\n" +
                content;
        out.write(response.getBytes());
    }

    private static void sendBinaryResponse(OutputStream out, String contentType, byte[] content) throws IOException {
        String headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n";
        out.write(headers.getBytes());
        out.write(content);
    }
}
