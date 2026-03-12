package org.example.ruledetect;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.example.utils.CollectionUtils;
import org.example.utils.JsonUtils;
import org.example.utils.ResourceUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductAccountTool implements Serializable {

    private static final String PATH_PRODUCT_ACCOUNT_INFO = ResourceUtils.getAbsolutePath("/product_account_rules.json");
    private static final ProductAccountTool ACCOUNT_NAME_TOOL = ProductAccountTool.getRuleFromJsonFile(PATH_PRODUCT_ACCOUNT_INFO);

    private static final String STR_GZH = "公众号";
    private static final String STR_GD = "挂短";
    private static final String STR_CH = "促活";

    private Map<String, String> product2accountPrefix;
//    private Map<String, String> accountPrefix2product;
    private Map<String, String> productMode2account;
    private Map<String,String> account2product;
    private Map<String,String> account2entranceMode;

    public ProductAccountTool(
            Map<String, String> product2accountPrefix, Map<String, String> productMode2account,
            Map<String,String> account2product, Map<String,String> account2entranceMode) {
        this.product2accountPrefix = product2accountPrefix;
        this.productMode2account = productMode2account;
        this.account2product = account2product;
        this.account2entranceMode = account2entranceMode;
//        this.accountPrefix2product = product2accountPrefix.entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public static final ProductAccountTool getInstance() {
        return ACCOUNT_NAME_TOOL;
    }

    Set<String> getProductSet() {
        return product2accountPrefix.keySet();
    }

    public String getAccountPrefix(String product) {
        String accountPrefix = null;
        if (product2accountPrefix.containsKey(product)) {
            accountPrefix = product2accountPrefix.get(product);
        }
        return accountPrefix;
    }

    public String getAccount(String product, String entranceMode) {
        String account = null;
        String key = getProductModeKey(product, entranceMode);
        if (productMode2account.containsKey(key)) {
            account = productMode2account.get(key);
        }
        return account;
    }

    public String getProduct(String account) {
        if (account2product.containsKey(account)) {
            return account2product.get(account);
        } else {
            String product = null;
            for (Map.Entry<String, String> entry : product2accountPrefix.entrySet()) {
                String tmpProduct = entry.getKey();
                String accountPrefix = entry.getValue();
                if (account.startsWith(accountPrefix)) {
                    product = tmpProduct;
                    break;
                }
            }
            return product;
        }
    }

    public String getEntranceMode(String account) {
        if (account2entranceMode.containsKey(account)) {
            return account2entranceMode.get(account);
        } else {
            if (account.endsWith("888") || account.endsWith("999")) {
                return STR_GZH;
            } else if (account.endsWith("555") || account.endsWith("666") || account.endsWith("661")) {
                return STR_GD;
            } else if (account.endsWith("111") || account.endsWith("333")) {
                return STR_CH;
            } else {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    private static String getProductModeKey(String account, String entranceMode) {
        return account + entranceMode;
    }

    private static ProductAccountTool getRuleFromJsonFile(String pathInputJson) {
        ProductAccountTool tool = null;
        try {
            String text = new String(Files.readAllBytes(Paths.get(pathInputJson)), StandardCharsets.UTF_8);
            Map<String, AccountInfo> product2AccountInfo = JsonUtils.fromJson(text, new TypeToken<Map<String, AccountInfo>>(){});

            Map<String, String> product2accountPrefix = new HashMap<>();
            Map<String, String> productMode2account = new HashMap<>();
            Map<String,String> account2product = new HashMap<>();
            Map<String,String> account2entranceMode = new HashMap<>();
            product2AccountInfo.forEach((product, accountInfo) -> {
                String accountPrefix = accountInfo.accountPrefix;
                if (accountPrefix != null) {
                    product2accountPrefix.put(product, accountPrefix);
                }
                List<String> gzhAccountList = accountInfo.getGzhAccountList();
                if (!CollectionUtils.isEmpty(gzhAccountList)) {
                    if (gzhAccountList.size() == 1) {
                        String gzhAccount = gzhAccountList.get(0);
                        String key = getProductModeKey(product, STR_GZH);
                        productMode2account.put(key, gzhAccount);
                    }
                    for (String account: gzhAccountList) {
                        account2product.put(account, product);
                        account2entranceMode.put(account, STR_GZH);
                    }
                }
                List<String> gdAccountList = accountInfo.getGdAccountList();
                if (!CollectionUtils.isEmpty(gdAccountList)) {
                    if (gdAccountList.size() == 1) {
                        String gdAccount = gdAccountList.get(0);
                        String key = getProductModeKey(product, STR_GD);
                        productMode2account.put(key, gdAccount);
                    }
                    for (String account: gdAccountList) {
                        account2product.put(account, product);
                        account2entranceMode.put(account, STR_GD);
                    }
                }
                List<String> chAccountList = accountInfo.getChAccountList();
                if (!CollectionUtils.isEmpty(chAccountList)) {
                    if (chAccountList.size() == 1) {
                        String chAccount = chAccountList.get(0);
                        String key = getProductModeKey(product, STR_CH);
                        productMode2account.put(key, chAccount);
                    }
                    for (String account: chAccountList) {
                        account2product.put(account, product);
                        account2entranceMode.put(account, STR_CH);
                    }
                }

                // 如果只有公众号或只有挂短，那么entranceMode可以为null
                if ((CollectionUtils.isEmpty(gzhAccountList) || CollectionUtils.isEmpty(gdAccountList)) && (!CollectionUtils.isEmpty(gzhAccountList) || !CollectionUtils.isEmpty(gdAccountList))) {
                    if (!CollectionUtils.isEmpty(gzhAccountList) && gzhAccountList.size() == 1) {
                        String gzhAccount = gzhAccountList.get(0);
                        String key = getProductModeKey(product, null);
                        productMode2account.put(key, gzhAccount);
                    }
                    if (!CollectionUtils.isEmpty(gdAccountList) && gdAccountList.size() == 1) {
                        String gdAccount = gdAccountList.get(0);
                        String key = getProductModeKey(product, null);
                        productMode2account.put(key, gdAccount);
                    }
                }
            });
            tool = new ProductAccountTool(product2accountPrefix, productMode2account, account2product, account2entranceMode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tool;
    }

    @Getter
    static class AccountInfo {

        @SerializedName("账号前缀")
        private String accountPrefix;
        @SerializedName("公众号账号")
        private List<String> gzhAccountList;
        @SerializedName("挂短账号")
        private List<String> gdAccountList;
        @SerializedName("促活账号")
        private List<String> chAccountList;
    }
}
