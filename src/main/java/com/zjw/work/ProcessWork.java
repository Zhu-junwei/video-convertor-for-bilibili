package com.zjw.work;

import com.zjw.config.ConvertProperties;
import com.zjw.domain.VideoInfo;
import com.zjw.system.ProcesInfo;
import com.zjw.utils.BiBiUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 朱俊伟
 * @since 2024/06/23 14:43
 */
@Component
@Slf4j
public class ProcessWork {

    @Resource
    private ConvertProperties convertProperties;
    private final List<VideoInfo> videoInfoList = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    public void work() {
        try {
            ProcesInfo.startTime = System.currentTimeMillis();
            initEnvironment();
            initAllVideoInfoList();

            // 创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (VideoInfo videoInfo : videoInfoList) {
                executorService.submit(() -> {
                    try {
                        // 执行音频视频合并方法
                        convertor(videoInfo.getVideoInputPath(), videoInfo.getAudioInputPath(), videoInfo.getVideoName());
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                });
            }

            // 关闭线程池
            executorService.shutdown();
            // 等待线程池中的所有线程执行完毕
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            log.info("转换完成，共计耗时:{},转换文件:{}个", ProcesInfo.getUptime(), videoInfoList.size());
            Thread.sleep(5000);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void initAllVideoInfoList() throws Exception {
        getAll(convertProperties.getVideoInputPath());
        videoInfoList.sort(Comparator.comparing(VideoInfo::getVideoName));
    }

    /**
     * 创建输入文件夹
     */
    private void initEnvironment() throws IOException {
        Path path = Path.of(convertProperties.getVideoOutPath());
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * 具体合成视频函数
     *
     * @param videoInputPath 原视频的全路径
     * @param audioInputPath 音频的全路径
     * @param videoName      合并后文件名字
     */
    public void convertor(String videoInputPath, String audioInputPath, String videoName) throws Exception {
        log.info(videoName);
        Process process = getProcess(videoInputPath, audioInputPath, videoName);

        try (InputStream errorStream = process.getErrorStream();
             InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
            while (br.readLine() != null) {}
        } catch (IOException e) {
            log.error("Error executing ffmpeg command for video: {}. Error: {}", videoName, e.getMessage());
        }
        process.waitFor();
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg command failed with exit code " + exitCode);
        }
    }

    private Process getProcess(String videoInputPath, String audioInputPath, String videoName) throws IOException {
        String videoOutPath = convertProperties.getVideoOutPath() + File.separator + videoName.replace(" ", "_");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                convertProperties.getFfmpegPath(),
                "-i", videoInputPath,
                "-i", audioInputPath,
                "-c:v", "copy",
                "-c:a", "aac",
                "-strict", "experimental",
                "-map", "0:v:0",
                "-map", "1:a:0",
                "-y", videoOutPath
        );

        return processBuilder.start();
    }

    public void getAll(String inPath) throws IOException {
        Path inputPath = Paths.get(inPath);

        if (Files.isDirectory(inputPath)) {
            Files.walkFileTree(inputPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith("audio.m4s")) {
                        String parentDir = file.getParent().toString();
                        String audioInputPath = parentDir + "/audio.m4s";
                        String videoInputPath = parentDir + "/video.m4s";
                        Path entryJsonPath = file.getParent().resolve("../entry.json").normalize();
                        if (Files.exists(entryJsonPath)) {
                            try (BufferedReader br = Files.newBufferedReader(entryJsonPath)) {
                                String fileJson = br.readLine();
                                String videoName = getFileName(fileJson);
                                videoInfoList.add(new VideoInfo(videoInputPath, audioInputPath, convertProperties.getVideoOutPath(), videoName));
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        ProcesInfo.totalVideoNum = videoInfoList.size();
    }

    public String getFileName(String fileJson) {
        String fileName;
        if (convertProperties.getPrefixFormat().getAddPrefixNum()) {
            int miniDigits = Objects.requireNonNullElse(
                    convertProperties.getPrefixFormat().getMiniDigits(),
                    String.valueOf(videoInfoList.size()).length()
            );
            fileName = fill(BiBiUtils.getVideoIndex(fileJson), miniDigits, Byte.MAX_VALUE) + "_" + BiBiUtils.getVideoName(fileJson) + ".mp4";
        } else {
            fileName = BiBiUtils.getVideoName(fileJson) + ".mp4";
        }
        return fileName;
    }

    /**
     * 生成类似 "001", "002", ..., "010" 的字符串
     */
    public String fill(int num, int min, int max) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 禁用数字格式化分组。 如：  000,001
        numberFormat.setGroupingUsed(false);
        // 保留最小位数
        numberFormat.setMinimumIntegerDigits(min);
        // 保留最大位数
        numberFormat.setMaximumIntegerDigits(max);
        return numberFormat.format(num);
    }
}
