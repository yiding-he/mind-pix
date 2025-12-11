package com.hyd.mindpix;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 应用程序配置
 */
@Data
public class MindPixConfig {

    private static MindPixConfig instance;

    /**
     * 最后打开的目录
     */
    private String lastOpenedDir;

    /**
     * 获取配置实例（单例）
     *
     * @return 配置实例
     */
    public static MindPixConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /**
     * 从默认配置文件加载配置
     *
     * @return 配置对象，如果文件不存在或解析失败则返回新的默认配置
     */
    private static MindPixConfig load() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            return new MindPixConfig();
        }

        try {
            String content = Files.readString(configPath);
            return JSON.parseObject(content, MindPixConfig.class, JSONReader.Feature.SupportSmartMatch);
        } catch (Exception e) {
            // 如果解析失败，返回默认配置
            return new MindPixConfig();
        }
    }

    /**
     * 保存配置到默认配置文件
     */
    public void save() {
        Path configPath = getConfigPath();
        try {
            // 确保父目录存在
            Path parent = configPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            String content = JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat);
            Files.writeString(configPath, content);
        } catch (IOException e) {
            // 保存失败，忽略（可能是权限问题）
            e.printStackTrace();
        }
    }

    /**
     * 获取配置文件路径
     *
     * @return 用户主目录下的 .mind-pix.json 文件路径
     */
    private static Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Path.of(userHome, ".mind-pix.json");
    }
}