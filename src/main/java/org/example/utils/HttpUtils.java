package org.example.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.utils.bean.HttpResponse;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class HttpUtils {


    private static final Set<String> IGNORE_LOG_URL_SET = Sets.newHashSet(ConstUtils.PULL_WECHAT_MSG_URL);
    private static final int CONNECT_TIMEOUT = 30 * 1000;
    private static final int READ_TIMEOUT = 90 * 1000;

    public static Map<String, String> POST_JSON_HEADER_MAP = ImmutableMap.of(
            "accept", "*/*",
            "connection", "Keep-Alive",
            "user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
            "Content-Type", "application/json;charset=utf-8"
    );

    public static HttpResponse doGet(String httpUrl, Map<String, Object> paramMap, Map<String, String> headerMap) {
        return doGet(httpUrl, paramMap, headerMap, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Http get请求
     * @param httpUrl 连接
     * @return 响应数据
     */
    public static HttpResponse doGet(
            String httpUrl, Map<String, Object> paramMap, Map<String, String> headerMap, int connectTimeout, int readTimeout){
        int rspCode = -1;
        //链接
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder result = new StringBuilder();
        try {
            if (paramMap != null && !paramMap.isEmpty()) {
                httpUrl += "?" + paramMap.entrySet().stream().map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.joining("&"));
            }
            //创建连接
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setConnectTimeout(connectTimeout);
            //设置读取超时时间
            connection.setReadTimeout(readTimeout);
            //设置请求头
            for (Map.Entry<String, String> entry: headerMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            log.info("start http get url: {}, param: {}, header: {}", httpUrl, JsonUtils.toJson(paramMap, false), JsonUtils.toJson(headerMap, false));
            //开始连接
            connection.connect();
            //获取响应数据
            rspCode = connection.getResponseCode();
//            if (rspCode == HttpURLConnection.HTTP_OK) {
            try {
                //获取返回的数据
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("end http get url: {}, param: {}, header: {}, exception: {}", httpUrl, JsonUtils.toJson(paramMap, false), JsonUtils.toJson(headerMap, false), e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("end http get url: {}, param: {}, header: {}, exception: {}", httpUrl, JsonUtils.toJson(paramMap, false), JsonUtils.toJson(headerMap, false), e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end http get url: {}, param: {}, header: {}, exception: {}", httpUrl, JsonUtils.toJson(paramMap, false), JsonUtils.toJson(headerMap, false), e);
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end http get url: {}, param: {}, header: {}, exception: {}", httpUrl, JsonUtils.toJson(paramMap, false), JsonUtils.toJson(headerMap, false), e);
                }
            }
            //关闭远程连接
            if (connection != null) {
                connection.disconnect();
            }
        }
        String rspText = result.toString();
        log.info("end http get url: {}, param: {}, header: {}, rspCode: {}, response: {}", httpUrl, JsonUtils.toJson(paramMap, false), JsonUtils.toJson(headerMap, false), rspCode, rspText);
        return new HttpResponse(rspCode, rspText);
    }

    public static HttpResponse doPost(String httpUrl, Object paramObject, Map<String, String> headerMap) {
        return doPost(httpUrl, paramObject, headerMap, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Http post请求
     * @param httpUrl 连接
     * @param paramObject 参数
     * @return
     */
    public static HttpResponse doPost(String httpUrl, Object paramObject, Map<String, String> headerMap, int connectTimeout, int readTimeout) {
        String param;
        if (paramObject instanceof String) {
            param = (String) paramObject;
        } else {
            param = JsonUtils.toJson(paramObject, false);
        }
        return doPost(httpUrl, param, headerMap, connectTimeout, readTimeout);
    }

    private static HttpResponse doPost(String httpUrl, String param, Map<String, String> headerMap) {
        return doPost(httpUrl, param, headerMap, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * Http post请求
     * @param httpUrl 连接
     * @param param 参数
     * @return
     */
    private static HttpResponse doPost(String httpUrl, String param, Map<String, String> headerMap, int connectTimeout, int readTimeout) {
        int rspCode = -1;
        StringBuilder result = new StringBuilder();
        //连接
        HttpURLConnection connection = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            //创建连接对象
            URL url = new URL(httpUrl);
            //创建连接
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            connection.setRequestMethod("POST");
            //设置连接超时时间
            connection.setConnectTimeout(connectTimeout);
            //设置读取超时时间
            connection.setReadTimeout(readTimeout);
            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            //设置是否可读取
            connection.setDoOutput(true);
            connection.setDoInput(true);
            //设置请求头
            for (Map.Entry<String, String> entry: headerMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            //拼装参数
            if (null != param && !param.equals("")) {
                //设置参数
                os = connection.getOutputStream();
                //拼装参数
                os.write(param.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            if (!IGNORE_LOG_URL_SET.contains(httpUrl)) {
                log.info("start http post url: {}, param: {}, header: {}", httpUrl, param, JsonUtils.toJson(headerMap, false));
            }
            //开启连接
            connection.connect();
            //读取响应
            rspCode = connection.getResponseCode();
//            if (rspCode == HttpURLConnection.HTTP_OK) {
            try {
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp).append("\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("end http post url: {}, param: {}, header: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap, false), e);
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("end http post url: {}, param: {}, header: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap, false), e);
        } finally {
            //关闭连接
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end http post url: {}, param: {}, header: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap, false), e);
                }
            }
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end http post url: {}, param: {}, header: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap, false), e);
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end http post url: {}, param: {}, header: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap, false), e);
                }
            }
            //关闭连接
            connection.disconnect();
        }
        String rspText = result.toString();
        if (!IGNORE_LOG_URL_SET.contains(httpUrl)) {
            log.info("end http post url: {}, param: {}, header: {}, rspCode: {}, response: {}", httpUrl, param, JsonUtils.toJson(headerMap, false), rspCode, rspText);
        }
        return new HttpResponse(rspCode, rspText);
    }

    public static HttpResponse doMultiPartFormPartPost(String url, Map<String, String> paramMap, Map<String, String> fileMap) {
        int rspCode = -1;
        String rspText = null;
        // 创建HttpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // 创建HttpPost对象
            HttpPost httpPost = new HttpPost(url);

            // 创建MultipartEntityBuilder对象
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE); // 解决中文乱码问题
            paramMap.forEach((key, value) -> {
                builder.addTextBody(key, value, ContentType.TEXT_PLAIN); // 添加文本字段
            });
            fileMap.forEach((key, pathFile) -> {
                builder.addBinaryBody(key, new File(pathFile)); // 添加文件字段
//                builder.addBinaryBody("file", new File(pathFile), ContentType.DEFAULT_BINARY, "filename"); // 文件名（可选）
            });
            HttpEntity multipart = builder.build();

            // 将构建的实体设置到HttpPost对象中
            httpPost.setEntity(multipart);

            log.info("start http multiPartFormPartPost url: {}, param: {}, fileMap: {}", url, JsonUtils.toJson(paramMap,false), JsonUtils.toJson(fileMap,false));

            // 执行请求并获取响应
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                rspCode = response.getStatusLine().getStatusCode();
//                System.out.println("Response Code : " + rspCode);
                HttpEntity responseEntity = response.getEntity();
                rspText = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
//                System.out.println("Response Body : " + rspText);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("end http multiPartFormPartPost url: {}, param: {}, fileMap: {}, exception: {}", url, JsonUtils.toJson(paramMap,false), JsonUtils.toJson(fileMap,false), e);
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("end http multiPartFormPartPost url: {}, param: {}, fileMap: {}, exception: {}", url, JsonUtils.toJson(paramMap,false), JsonUtils.toJson(fileMap,false), e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("end http multiPartFormPartPost url: {}, param: {}, fileMap: {}, exception: {}", url, JsonUtils.toJson(paramMap,false), JsonUtils.toJson(fileMap,false), e);
            }
        }
        log.info("end http multiPartFormPartPost url: {}, param: {}, fileMap: {}, rspCode: {}, response: {}", url, JsonUtils.toJson(paramMap,false), JsonUtils.toJson(fileMap,false), rspCode, rspText);
        return new HttpResponse(rspCode, rspText);
    }

    public static boolean download(String httpUrl, Map<String, String> headerMap, String pathOutput) {
        BufferedInputStream in = null;
        FileOutputStream out = null;

        try {
            URL downloadUrl = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            //设置读取超时时间
            connection.setReadTimeout(READ_TIMEOUT);
            //设置通用的请求属性
            for (Map.Entry<String, String> entry: headerMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            log.info("start download http get url: {}, headerMap: {}, pathOutput: {}", httpUrl, JsonUtils.toJson(headerMap,false), pathOutput);

            //开启连接
            connection.connect();
            int rspCode = connection.getResponseCode();
            if (rspCode == HttpURLConnection.HTTP_OK) {
                File file = new File(pathOutput);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                in = new BufferedInputStream(connection.getInputStream());
                out = new FileOutputStream(pathOutput);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

//            System.out.println("文件下载成功，保存到: " + pathOutput);
                log.info("end download http get url: {}, headerMap: {}, pathOutput: {}", httpUrl, JsonUtils.toJson(headerMap,false), pathOutput);
                return true;
            } else {
                log.error("end download http get url: {}, headerMap: {}, pathOutput: {}, rspCode: {}", httpUrl, JsonUtils.toJson(headerMap,false), pathOutput, rspCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("end download http get url: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, JsonUtils.toJson(headerMap,false), pathOutput, e);
            return false;
        } finally {
            // 确保所有资源被关闭
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("end download http get url: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, JsonUtils.toJson(headerMap,false), pathOutput, e);
                    System.err.println("关闭输入流时出错: " + e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("end download http get url: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, JsonUtils.toJson(headerMap,false), pathOutput, e);
                    System.err.println("关闭输出流时出错: " + e.getMessage());
                }
            }
        }
    }

    public static boolean download(String httpUrl, String param, Map<String, String> headerMap, String pathOutput) {
        BufferedInputStream in = null;
        FileOutputStream out = null;
        OutputStream os = null;

        try {
            URL downloadUrl = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            //设置请求方式
            connection.setRequestMethod("POST");
            //设置连接超时时间
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            //设置读取超时时间
            connection.setReadTimeout(READ_TIMEOUT);
            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            //设置是否可读取
            connection.setDoOutput(true);
            connection.setDoInput(true);
            //设置通用的请求属性
            for (Map.Entry<String, String> entry: headerMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            //拼装参数
            if (null != param && !param.equals("")) {
                //设置参数
                os = connection.getOutputStream();
                //拼装参数
                os.write(param.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            log.info("start download http post url: {}, param: {}, headerMap: {}, pathOutput: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput);

            //开启连接
            connection.connect();
            int rspCode = connection.getResponseCode();
            if (rspCode == HttpURLConnection.HTTP_OK) {

                in = new BufferedInputStream(connection.getInputStream());
                out = new FileOutputStream(pathOutput);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

//            System.out.println("文件下载成功，保存到: " + pathOutput);
                log.info("end download http post url: {}, param: {}, headerMap: {}, pathOutput: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput);
                return true;
            } else {
                log.error("end download http post url: {}, param: {}, headerMap: {}, pathOutput: {}, rspCode: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput, rspCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("end download http post url: {}, param: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput, e);
            return false;
        } finally {
            // 确保所有资源被关闭
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end download http post url: {}, param: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput, e);
                }
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end download http post url: {}, param: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput, e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("end download http post url: {}, param: {}, headerMap: {}, pathOutput: {}, exception: {}", httpUrl, param, JsonUtils.toJson(headerMap,false), pathOutput, e);
                }
            }
        }
    }

    public static void main(String[] args) {
//        String message = doGet("https://www.ifeng.com/");
//        System.out.println(message);

//        HttpResponse rsp = doPost("https://bkzsdata.ecust.edu.cn/lqxx/s/api/front/lqxx2/getList?type=lnfs", "[{\"field\":\"sf\",\"value\":\"上海\"},{\"field\":\"nf\",\"value\":\"2023\"},{\"field\":\"klmc\",\"value\":\"全部\"},{\"field\":\"zslb\",\"value\":\"全部\"},{\"field\":\"pcmc\",\"value\":\"\"},{\"field\":\"xkyq\",\"value\":\"全部\"}]", HttpUtils.POST_JSON_HEADER_MAP);
//        HttpResponse rsp = doMultiPartFormPartPost("http://192.168.23.85:8860/market/AiSpeech/admin/login", ImmutableMap.of("account", "admin", "password", "admin"), Collections.emptyMap());
//        System.out.println(rsp.getText());
//        download("http://192.168.23.85/baize.ico", "data/baize.ico");
    }
}