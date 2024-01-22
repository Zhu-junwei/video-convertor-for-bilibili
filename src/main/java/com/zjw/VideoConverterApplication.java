package com.zjw;

import com.zjw.config.ConvertProperties;
import com.zjw.domain.VideoInfo;
import com.zjw.utils.BiBiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableConfigurationProperties(ConvertProperties.class)
@Slf4j
public class VideoConverterApplication {

    private static ConvertProperties convertProperties;
    static List<VideoInfo> videoInfoList = new ArrayList<>();

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(VideoConverterApplication.class, args);
        convertProperties = context.getBean(ConvertProperties.class);
        work();
    }

    public static void work() {
        try {
            Long startTime = System.currentTimeMillis();
            initEnvironment();
            initAllVideoInfoList();

            // 创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (VideoInfo videoInfo : videoInfoList) {
                // 提交任务到线程池
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
            Long endTime = System.currentTimeMillis();
            log.info("转换完成，共计耗时:" + (endTime - startTime) / 1000 + "秒,转换文件:" + videoInfoList.size() + "个");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void initAllVideoInfoList() throws Exception {
        getAll(convertProperties.getVideoInputPath());
        videoInfoList.sort((o1, o2) -> o1.getVideoName().compareTo(o2.getVideoName()));
    }

    /**
     * 创建输入文件夹
     */
    private static void initEnvironment() throws Exception {
        File file = new File(convertProperties.getVideoOutPath());
        if (!file.exists())
            file.mkdirs();
    }

    /**
     * 具体合成视频函数
     *
     * @param videoInputPath 原视频的全路径
     * @param audioInputPath 音频的全路径
     * @param videoName      合并后文件名字
     */
    public static void convertor(String videoInputPath, String audioInputPath, String videoName)
            throws Exception {
        log.info(videoName);
        Process process = getProcess(videoInputPath, audioInputPath, videoName);

        try (InputStream errorStream = process.getErrorStream();
             InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
            // 用来收集错误信息的
            String str = "";
            while ((str = br.readLine()) != null) {
//                System.out.println(str);
            }
            process.waitFor();
        } catch (IOException e) {
            // 处理异常，记录到日志或抛出
            log.error("Error executing ffmpeg command", e);
        }
    }

    private static Process getProcess(String videoInputPath, String audioInputPath, String videoName) throws IOException {
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

    /**
     * 递归函数
     *
     * @param inPath 输入路径
     * @throws Exception 异常
     */
    public static void getAll(String inPath) throws Exception {
        String videoInputPath = "";
        String audioInputPath = "";

        File file = new File(inPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File f : files) {
                getAll(f.getPath());
                if (f.isFile()) {
                    if (f.getName().endsWith("audio.m4s")) {
                        audioInputPath = file.getPath() + "/audio.m4s";
                        videoInputPath = file.getPath() + "/video.m4s";
                        //获取输出的文件名
                        BufferedReader br = new BufferedReader(new FileReader(new File(file.getParentFile()
                                .getPath() + "/entry.json")));
                        String fileJson = br.readLine();
                        br.close();
                        String videoName = getFileName(fileJson);
                        videoInfoList.add(new VideoInfo(videoInputPath, audioInputPath, convertProperties.getVideoOutPath(), videoName));
                    }

                }

            }

        }
    }

    public static String getFileName(String fileJson) {
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
    public static String fill(int num, int min, int max) {
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
