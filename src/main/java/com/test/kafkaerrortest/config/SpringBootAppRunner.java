package com.test.kafkaerrortest.config;

import java.io.IOException;

public class SpringBootAppRunner {

    public static void main(String[] args) {
        SpringBootAppRunner runner = new SpringBootAppRunner();
        runner.runExternalSpringBootApp();
    }

    public void runExternalSpringBootApp() {
        // 외부 JAR 파일의 경로
        String jarPath = "/path/to/other-spring-boot-app.jar";

        // 명령어를 구성합니다.
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);

        // 프로세스의 표준 출력과 표준 에러 출력을 합칩니다.
        processBuilder.redirectErrorStream(true);

        try {
            // 프로세스를 시작합니다.
            Process process = processBuilder.start();

            // 프로세스가 종료될 때까지 기다립니다.
            int exitCode = process.waitFor();
            System.out.println("External Spring Boot app exited with code: " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

