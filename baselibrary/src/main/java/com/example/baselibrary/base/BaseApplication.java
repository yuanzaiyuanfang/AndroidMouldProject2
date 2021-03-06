package com.example.baselibrary.base;

import android.content.Context;
import android.os.Build;

import com.alibaba.sdk.android.httpdns.DegradationFilter;
import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.basemodule.base.IBaseApplication;
import com.basemodule.local.sharedpref.SharedPrefUtils;
import com.basemodule.utils.log.MyLogUtil;
import com.blankj.utilcode.util.LogUtils;
import com.example.baselibrary.R;
import com.example.baselibrary.constant.APPConstant;
import com.example.baselibrary.okgo.HttpDNSInterceptor;
import com.example.baselibrary.util.MyToastUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.store.PersistentCookieStore;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;
import com.taobao.sophix.util.PatchStatus;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * add your personal code here
 * description:
 * Date: 2017/4/6 16:56
 * User: Administrator
 */
public class BaseApplication extends IBaseApplication {

    //##########################  custom variables start ##########################################

    //=== httpdns
    private static HttpDnsService httpdns; // ali httpdns

    // httpdns预解析域名
    private ArrayList hostList = new ArrayList<>(Arrays.asList("www.aliyun.com", "www.taobao.com"));

    //=== okgo参数设置
    private static int DEF_CONNECT_TIMEOUT = 20000; // 默认的连接超时

    private static int DEF_READ_TIMEOUT = 20000; // 默认的连接超时

    private static int DEF_WRITE_TIMEOUT = 20000; // 默认的连接超时

    private RefWatcher refWatcher;

    //##########################   custom variables end  ##########################################

    //######################  override methods start ##############################################

    @Override
    public void onCreate() {
        super.onCreate();
        // 自定义toast
        MyToastUtil.init(true, true);
        // 自定义log
        MyLogUtil.init("TRUE".equals(getResources().getString(R.string.APP_IS_SHOW_LOG)) ? true : false, "MODULE_PROJECT", true);
        //=== init hotfix
        initHotFix();
        //=== okgo
        initOkGo();
        //=== ali httpdns
        initHttpDns(APPConstant.ALI_HTTPDNS_ACCOUND_ID, hostList);
        //=== 注册App异常崩溃处理器
        registerUncaughtExceptionHandler(APPConstant.DEFAULT_CRASH_FILE_PATH);
        //===
        SharedPrefUtils.init(getApplicationContext());

        //=== 内存泄露检测框架
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);

        MyLogUtil.i("packageName = " + getPackageInfo().packageName);
        // 展示配置的一些参数
        getMainFestMetaProperty();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    //######################   override methods end  ##############################################

    //###################### override custom metohds start ########################################

    //######################  override custom metohds end  ########################################

    //######################      custom metohds start     ########################################

    /**
     * okgo初始化
     */
    protected void initOkGo() {

        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文
        headers.put("commonHeaderKey2", "commonHeaderValue2");
        HttpParams params = new HttpParams();
        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        params.put("commonParamsKey2", "这里支持中文参数");
        //-----------------------------------------------------------------------------------//

        //必须调用初始化
        OkGo.init(this);

        //以下设置的所有参数是全局参数,同样的参数可以在请求的时候再设置一遍,那么对于该请求来讲,请求中的参数会覆盖全局参数
        //好处是全局参数统一,特定请求可以特别定制参数
        try {
            //以下都不是必须的，根据需要自行选择,一般来说只需要 debug,缓存相关,cookie相关的 就可以了
            OkGo.getInstance()
                    // 打开该调试开关,打印级别INFO,并不是异常,是为了显眼,不需要就不要加入该行
                    // 最后的true表示是否打印okgo的内部异常，一般打开方便调试错误
                    .debug("OkGo", Level.INFO, true)

                    //如果使用默认的 60秒,以下三行也不需要传
                    .setConnectTimeout(DEF_CONNECT_TIMEOUT)  //全局的连接超时时间
                    .setReadTimeOut(DEF_READ_TIMEOUT)     //全局的读取超时时间
                    .setWriteTimeOut(DEF_WRITE_TIMEOUT)    //全局的写入超时时间

                    //可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy/
                    .setCacheMode(CacheMode.NO_CACHE)

                    //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)

                    //可以全局统一设置超时重连次数,默认为三次,那么最差的情况会请求4次(一次原始请求,三次重连请求),不需要可以设置为0
                    .setRetryCount(3)

                    //如果不想让框架管理cookie（或者叫session的保持）,以下不需要
//              .setCookieStore(new MemoryCookieStore())            //cookie使用内存缓存（app退出后，cookie消失）
                    .setCookieStore(new PersistentCookieStore())        //cookie持久化存储，如果cookie不过期，则一直有效

                    //可以设置https的证书,以下几种方案根据需要自己设置
                    .setCertificates()                                  //方法一：信任所有证书,不安全有风险
//              .setCertificates(new SafeTrustManager())            //方法二：自定义信任规则，校验服务端证书
//              .setCertificates(getAssets().open("srca.cer"))      //方法三：使用预埋证书，校验服务端证书（自签名证书）
//              //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
//               .setCertificates(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"))//

                    //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
//               .setHostnameVerifier(new SafeHostnameVerifier())

                    //可以添加全局拦截器，不需要就不要加入，错误写法直接导致任何回调不执行
                    .addInterceptor(new HttpDNSInterceptor())

                    //这两行同上，不需要就不要加入
                    .addCommonHeaders(headers)  //设置全局公共头
                    .addCommonParams(params);   //设置全局公共参数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     * 重要的事情说三遍，以下代码不要直接使用
     */
    private static class SafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                for (X509Certificate certificate : chain) {
                    certificate.checkValidity(); //检查证书是否过期，签名是否通过等
                }
            } catch (Exception e) {
                throw new CertificateException(e);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     * 重要的事情说三遍，以下代码不要直接使用
     */
    private static class SafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //验证主机名是否匹配
//            return hostname.equals("server.jeasonlzy.com");
            return true;
        }
    }

    /**
     * Hotfix 热修复
     * initialize的调用应该尽可能的早. 强烈推荐在Application.onCreate()中进行SDk初始化以及查询服务器是否有可用补丁的操作.
     */
    protected void initHotFix() {
        SophixManager.getInstance().setContext(this)
                .setAppVersion(getPackageInfo().versionName)
                .setAesKey(null)
                .setEnableDebug(true)
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onload(final int mode, final int code, final String info, final int handlePatchVersion) {
                        // 补丁加载回调通知
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            // 表明补丁加载成功
                            LogUtils.i("HotFixManager--补丁加载成功");
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            // 表明新补丁生效需要重启. 开发者可提示用户或者强制重启;
                            // 建议: 用户可以监听进入后台事件, 然后应用自杀
                            LogUtils.i("HotFixManager--新补丁生效需要重启. 业务方可自行实现逻辑, 提示用户或者强制重启, 可以监听应用进入后台事件, 然后应用自杀");
                        } else if (code == PatchStatus.CODE_LOAD_FAIL) {
                            // 内部引擎异常, 推荐此时清空本地补丁, 防止失败补丁重复加载
                            // SophixManager.getInstance().cleanPatches();
                            LogUtils.i("HotFixManager--内部引擎异常, 推荐此时清空本地补丁, 防止失败补丁重复加载");
                        } else {
                            // 其它错误信息, 查看PatchStatus类说明
                            LogUtils.i("HotFixManager--其它错误信息, 查看PatchStatus类说明");
                        }
                    }
                }).initialize();
        SophixManager.getInstance().queryAndLoadNewPatch();
    }

    /**
     * ali httpdns初始化
     *
     * @param accountID dns账号
     * @param hostList  与解析域名列表 new ArrayList<>(Arrays.asList("www.aliyun.com", "www.taobao.com"))
     */
    protected void initHttpDns(String accountID, ArrayList hostList) {
        try {
            // 设置APP Context和Account ID，并初始化HTTPDNS
            httpdns = HttpDns.getService(getApplicationContext(), accountID);
            // DegradationFilter用于自定义降级逻辑
            // 通过实现shouldDegradeHttpDNS方法，可以根据需要，选择是否降级
            DegradationFilter filter = new DegradationFilter() {
                @Override
                public boolean shouldDegradeHttpDNS(String hostName) {
                    // 此处可以自定义降级逻辑，例如www.taobao.com不使用HttpDNS解析
                    // 参照HttpDNS API文档，当存在中间HTTP代理时，应选择降级，使用Local DNS
                    return hostName.equals("www.taobao.com") || detectIfProxyExist(getApplicationContext());
                }
            };
            // 将filter传进httpdns，解析时会回调shouldDegradeHttpDNS方法，判断是否降级
            httpdns.setDegradationFilter(filter);
            // 设置预解析域名列表
            httpdns.setPreResolveHosts(hostList);

            // 示例
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 发送网络请求
                        String originalUrl = "http://www.aliyun.com";
                        URL url = new URL(originalUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        // 异步接口获取IP
                        String ip = httpdns.getIpByHostAsync(url.getHost());

                        if (ip != null) {
                            // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
                            LogUtils.d("Get IP: " + ip + " for host: " + url.getHost() + " from HTTPDNS successfully!");
                            String newUrl = originalUrl.replaceFirst(url.getHost(), ip);
                            conn = (HttpURLConnection) new URL(newUrl).openConnection();
                            // 设置HTTP请求头Host域
                            conn.setRequestProperty("Host", url.getHost());
                        }
                        DataInputStream dis = new DataInputStream(conn.getInputStream());
                        int len;
                        byte[] buff = new byte[4096];
                        StringBuilder response = new StringBuilder();
                        while ((len = dis.read(buff)) != -1) {
                            response.append(new String(buff, 0, len, Charset.defaultCharset()));
                        }
                        LogUtils.d("Response: " + response.toString());

                        // 允许返回过期的IP
                        httpdns.setExpiredIPEnabled(true);
                        // 异步接口获取IP
                        ip = httpdns.getIpByHostAsync(url.getHost());
                        if (ip != null) {
                            // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
                            LogUtils.d("Get IP: " + ip + " for host: " + url.getHost() + " from HTTPDNS successfully!");
                            String newUrl = originalUrl.replaceFirst(url.getHost(), ip);
                            conn = (HttpURLConnection) new URL(newUrl).openConnection();
                            // 设置HTTP请求头Host域
                            conn.setRequestProperty("Host", url.getHost());
                        }
                        len = 0;
                        response = new StringBuilder();
                        dis = new DataInputStream(conn.getInputStream());
                        while ((len = dis.read(buff)) != -1) {
                            response.append(new String(buff, 0, len, Charset.defaultCharset()));
                        }
                        LogUtils.d("Response: " + response.toString());

                        // 测试黑名单中的域名
                        ip = httpdns.getIpByHostAsync("www.taobao.com");
                        if (ip == null) {
                            LogUtils.d("由于在降级策略中过滤了www.taobao.com，无法从HTTPDNS服务中获取对应域名的IP信息");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测系统是否已经设置代理，请参考HttpDNS API文档。
     */
    public static boolean detectIfProxyExist(Context ctx) {
        boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyHost;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyHost = System.getProperty("http.proxyHost");
            String port = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt(port != null ? port : "-1");
        } else {
            proxyHost = android.net.Proxy.getHost(ctx);
            proxyPort = android.net.Proxy.getPort(ctx);
        }
        return proxyHost != null && proxyPort != -1;
    }

    /**
     * leakcanary
     *
     * @param context
     * @return
     */
    public static RefWatcher getRefWatcher(Context context) {
        BaseApplication application = (BaseApplication) context.getApplicationContext();
        return application.refWatcher;
    }


    public static HttpDnsService getHttpdns() {
        return httpdns;
    }

    public static void setHttpdns(HttpDnsService httpdns) {
        BaseApplication.httpdns = httpdns;
    }

    /**
     * 获取manifest中的参数配置
     */
    private void getMainFestMetaProperty() {
//        try {
//            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
//                    PackageManager.GET_META_DATA);
//            MyLogUtil.i("UMENG_APPKEY = " + appInfo.metaData.getString("UMENG_APPKEY"));
//            String UMENG_CHANNEL_VALUE = AnalyticsConfig.getChannel(getApplicationContext());
//            MyLogUtil.i("UMENG_CHANNEL_VALUE = " + UMENG_CHANNEL_VALUE);
//            MyLogUtil.i("package name = " + this.getPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    //######################    custom metohds end   ##############################################
}
