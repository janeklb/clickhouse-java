package com.clickhouse.client.api;

import com.clickhouse.client.api.internal.ClickHouseLZ4OutputStream;
import com.clickhouse.data.ClickHouseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Enumerates all client properties that are known at release.
 */
public enum ClientConfigProperties {

    SESSION_DB_ROLES("session_db_roles", List.class),

    SETTING_LOG_COMMENT(serverSetting("log_comment"), String.class),

    HTTP_USE_BASIC_AUTH("http_use_basic_auth", Boolean.class, "true"),

    USER("user", String.class, "default"),

    PASSWORD("password", String.class),

    /**
     * Maximum number of active connection in internal connection pool.
     */
    HTTP_MAX_OPEN_CONNECTIONS("max_open_connections", Integer.class, "10"),

    /**
     * HTTP keep-alive timeout override.
     */
    HTTP_KEEP_ALIVE_TIMEOUT("http_keep_alive_timeout", Long.class),

    USE_SERVER_TIMEZONE("use_server_time_zone", Boolean.class, "true"),

    USE_TIMEZONE("use_time_zone", TimeZone.class),

    SERVER_VERSION("server_version", String.class),

    SERVER_TIMEZONE("server_time_zone", TimeZone.class, "UTC"),

    ASYNC_OPERATIONS("async", Boolean.class, "false"),

    CONNECTION_TTL("connection_ttl", Long.class, "-1"),

    CONNECTION_TIMEOUT("connection_timeout", Long.class),

    CONNECTION_REUSE_STRATEGY("connection_reuse_strategy", ConnectionReuseStrategy.class, String.valueOf(ConnectionReuseStrategy.FIFO)),

    SOCKET_OPERATION_TIMEOUT("socket_timeout", Integer.class, "0"),

    SOCKET_RCVBUF_OPT("socket_rcvbuf", Integer.class, "804800"),

    SOCKET_SNDBUF_OPT("socket_sndbuf",  Integer.class,"804800"),

    SOCKET_REUSEADDR_OPT("socket_reuseaddr", Boolean.class),

    SOCKET_KEEPALIVE_OPT("socket_keepalive", Boolean.class),

    SOCKET_TCP_NO_DELAY_OPT("socket_tcp_nodelay", Boolean.class),

    SOCKET_LINGER_OPT("socket_linger", Boolean.class),

    DATABASE("database", String.class, "default"),

    COMPRESS_SERVER_RESPONSE("compress", Boolean.class, "true"), // actually a server setting, but has client effect too

    COMPRESS_CLIENT_REQUEST("decompress", Boolean.class, "false"), // actually a server setting, but has client effect too

    USE_HTTP_COMPRESSION("client.use_http_compression", Boolean.class, "false"),

    COMPRESSION_LZ4_UNCOMPRESSED_BUF_SIZE("compression.lz4.uncompressed_buffer_size", Integer.class, String.valueOf(ClickHouseLZ4OutputStream.UNCOMPRESSED_BUFF_SIZE)),

    DISABLE_NATIVE_COMPRESSION("disable_native_compression", Boolean.class, "false"),

    PROXY_TYPE("proxy_type", String.class), // "http"

    PROXY_HOST("proxy_host", String.class),

    PROXY_PORT("proxy_port", Integer.class),

    PROXY_USER("proxy_user", String.class),

    PROXY_PASSWORD("proxy_password", String.class),

    MAX_EXECUTION_TIME("max_execution_time", Integer.class,"0"),

    SSL_TRUST_STORE("trust_store", String.class),

    SSL_KEYSTORE_TYPE("key_store_type", String.class),

    SSL_KEY_STORE("ssl_key_store", String.class),

    SSL_KEY_STORE_PASSWORD("key_store_password",  String.class),

    SSL_KEY("ssl_key",  String.class),

    CA_CERTIFICATE("sslrootcert", String.class),

    SSL_CERTIFICATE("sslcert", String.class),

    RETRY_ON_FAILURE("retry", Integer.class, "3"),

    INPUT_OUTPUT_FORMAT("format", ClickHouseFormat.class),

    MAX_THREADS_PER_CLIENT("max_threads_per_client", Integer.class, "0"),

    QUERY_ID("query_id", String.class), // actually a server setting, but has client effect too

    CLIENT_NETWORK_BUFFER_SIZE("client_network_buffer_size", Integer.class, "300000"),

    ACCESS_TOKEN("access_token", String.class),

    SSL_AUTH("ssl_authentication", Boolean.class, "false"),

    CONNECTION_POOL_ENABLED("connection_pool_enabled",  Boolean.class, "true"),

    CONNECTION_REQUEST_TIMEOUT("connection_request_timeout", Long.class, "10000"),

    CLIENT_RETRY_ON_FAILURE("client_retry_on_failures", List.class,
            String.join(",", ClientFaultCause.NoHttpResponse.name(), ClientFaultCause.ConnectTimeout.name(),
            ClientFaultCause.ConnectionRequestTimeout.name(), ClientFaultCause.ServerRetryable.name())) {
        @Override
        public Object parseValue(String value) {
            List<String> strValues = (List<String>) super.parseValue(value);
            List<ClientFaultCause> failures = new ArrayList<ClientFaultCause>();
            if (strValues != null) {
                for (String strValue : strValues) {
                    failures.add(ClientFaultCause.valueOf(strValue));
                }
            }
            return failures;
        }
    },

    CLIENT_NAME("client_name", String.class, ""),

    /**
     * An old alias to {@link ClientConfigProperties#CLIENT_NAME}. Using the last one is preferred.
     */
    @Deprecated
    PRODUCT_NAME("product_name", String.class),

    BEARERTOKEN_AUTH ("bearer_token", String.class),
    /**
     * Indicates that data provided for write operation is compressed by application.
     */
    APP_COMPRESSED_DATA("app_compressed_data", Boolean.class, "false"),

    /**
     * Name of the group under which client metrics appear
     */
    METRICS_GROUP_NAME("metrics_name", String.class, "ch-http-pool"),

    HTTP_SAVE_COOKIES("client.http.cookies_enabled",  Boolean.class, "false"),

    BINARY_READER_USE_PREALLOCATED_BUFFERS("client_allow_binary_reader_to_reuse_buffers", Boolean.class, "false"),

    ;

    private static final Logger LOG = LoggerFactory.getLogger(ClientConfigProperties.class);

    private final String key;

    private final Class<?> valueType;

    private final String defaultValue;

    private final Object defaultObjValue;

    ClientConfigProperties(String key, Class<?> valueType) {
        this(key, valueType, null);
    }

    ClientConfigProperties(String key, Class<?> valueType, String defaultValue) {
        this.key = key;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.defaultObjValue = parseValue(defaultValue);
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefObjVal() {
        return (T) defaultObjValue;
    }

    public static final String HTTP_HEADER_PREFIX = "http_header_";

    public static final String SERVER_SETTING_PREFIX = "clickhouse_setting_";

    public static String serverSetting(String key) {
        return SERVER_SETTING_PREFIX + key;
    }

    public static String httpHeader(String key) {
        return HTTP_HEADER_PREFIX + key.toUpperCase(Locale.US);
    }

    public static String commaSeparated(Collection<?> values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(value.toString().replaceAll(",", "\\\\,")).append(",");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static List<String> valuesFromCommaSeparated(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split("(?<!\\\\),")).map(s -> s.replaceAll("\\\\,", ","))
                .collect(Collectors.toList());
    }

    public Object parseValue(String value) {
        if (value == null) {
            return null;
        }

        if (valueType.equals(String.class)) {
            return value;
        }

        if (valueType.equals(Boolean.class)) {
            if (value.equals("1")) return true;
            if (value.equals("0")) return false;
            return Boolean.parseBoolean(value);
        }

        if (valueType.equals(Integer.class)) {
            return Integer.parseInt(value);
        }

        if (valueType.equals(Long.class)) {
            return Long.parseLong(value);
        }

        if (valueType.equals(List.class)) {
            return valuesFromCommaSeparated(value);
        }

        if (valueType.isEnum()) {
            Object[] constants = valueType.getEnumConstants();
            for (Object constant : constants) {
                if (constant.toString().equals(value)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid constant name '" + value + "' for enum " + valueType.getName());
        }

        if (valueType.equals(TimeZone.class)) {
            return TimeZone.getTimeZone(value);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(Map<String, Object> configMap) {
        return (T) configMap.getOrDefault(getKey(), getDefObjVal());
    }

    public <T> void applyIfSet(Map<String, Object> configMap, Consumer<T> consumer) {
        T value = (T) configMap.get(getKey());
        if  (value != null) {
            consumer.accept(value);
        }
    }

    public static Map<String, Object> parseConfigMap(Map<String, String> configMap) {
        Map<String, Object> parsedConfig = new HashMap<>();

        Map<String, String> tmpMap = new HashMap<>(configMap);

        for (ClientConfigProperties config : ClientConfigProperties.values()) {
            String value = tmpMap.remove(config.getKey());
            if (value != null) {
                parsedConfig.put(config.getKey(), config.parseValue(value));
            }
        }

        for (String key : new HashSet<>(tmpMap.keySet())) {
            if (key.startsWith(HTTP_HEADER_PREFIX) || key.startsWith(SERVER_SETTING_PREFIX)) {
                parsedConfig.put(key, tmpMap.remove(key));
            }
        }

        LOG.warn("Unknown and unmapped config properties: {}", tmpMap);

        return parsedConfig;
    }
}
