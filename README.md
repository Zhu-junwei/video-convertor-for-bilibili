# 合并哔哩哔哩下载的音视频工具

## 简介
通过ffmpeg批量合并音视频文件，重命名文件，自定义合成文件路径。

## 使用说明
### 前期准备

- 安装 [ffmpeg](https://ffmpeg.org/) 工具。
### 拷贝下载哔哩哔哩文件至电脑

**bilibili默认缓存路径 \Android\data\tv.danmaku.bili\download**

需要用到缓存里的下列文件

- audio.m4s -> 视频文件

- video.m4s -> 音频文件

- entry.json -> 视频信息

### 运行VideoConverterApplication.java中的main方法

运行前修改application.yml中的参数
```yml
convert:
  ffmpeg-path: D:/ffmpeg/bin/ffmpeg.exe
  video-input-path: F:/682703844
  video-out-path: F:/合并
  prefix-format:            #文件名前缀格式化
    add-prefix-num: true    #添加数字前缀，默认不添加
    mini-digits: 3          #设置数字编号的位数，不设置默认按照视频的数量设置位数
```

## Q&A

- 解析失败了怎么办？

> 你可能需要debug哪一步错了。