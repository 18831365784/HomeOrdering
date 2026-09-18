package com.homeordering.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 本地上传文件 URL：库内只存 /uploads/...，对外用当前 server.url 拼绝对地址。
 * 微信头像等外链原样保留。
 */
@Component
public class FileUrlHelper {

    private static final String UPLOADS_MARKER = "/uploads/";

    @Value("${server.url}")
    private String serverUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 写入数据库前：若为本站上传资源，剥掉主机与 context-path，只留 /uploads/...
     */
    public String toStoredPath(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        int idx = url.indexOf(UPLOADS_MARKER);
        if (idx >= 0) {
            return url.substring(idx);
        }
        return url.trim();
    }

    /**
     * 返回给前端：本站 /uploads/...（或旧的绝对上传地址）用当前 server.url 重写；外链不动。
     */
    public String toPublicUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return stored;
        }
        String value = stored.trim();
        int idx = value.indexOf(UPLOADS_MARKER);
        if (idx >= 0) {
            return publicBase() + value.substring(idx);
        }
        return value;
    }

    private String publicBase() {
        String base = serverUrl == null ? "" : serverUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String ctx = contextPath == null ? "" : contextPath.trim();
        if (!ctx.isEmpty() && !ctx.startsWith("/")) {
            ctx = "/" + ctx;
        }
        if (ctx.endsWith("/")) {
            ctx = ctx.substring(0, ctx.length() - 1);
        }
        return base + ctx;
    }
}
