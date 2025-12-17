package com.zj.caseswitcher.utils;

import com.zj.caseswitcher.enums.CaseModelEnum;
import com.zj.caseswitcher.interfaces.ICaseModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 可读性优化的转换功能测试
 * 采用清晰的结构和命名，提高代码可维护性
 *
 * @author : jie.zhou
 * @date : 2025/12/17
 */
public class CaseConversionReadableTest {

    /**
     * 测试辅助方法：执行转换并验证结果
     */
    private void assertConversion(String input, CaseModelEnum conversionType, String expected) {
        ICaseModel converter = conversionType.getConvert();
        String result = converter.convert(input);
        assertEquals(
                String.format("%s 转换失败: 输入='%s', 期望='%s', 实际='%s'", 
                        conversionType.getName(), input, expected, result),
                expected, result);

    }

    // =======================================
    // 基本命名格式测试
    // =======================================

    @Test
    public void testCamelCaseConversion() {
        // 测试输入: camelCase
        String input = "camelCase";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCase");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCase");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE");
        assertConversion(input, CaseModelEnum.DASH, "camel-case");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "CAMEL-CASE");
        assertConversion(input, CaseModelEnum.BLANK, "camel case");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "Camel Case");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "CAMEL CASE");
    }

    @Test
    public void testSnakeCaseConversion() {
        // 测试输入: camel_case
        String input = "camel_case";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCase");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCase");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE");
        assertConversion(input, CaseModelEnum.DASH, "camel-case");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "CAMEL-CASE");
        assertConversion(input, CaseModelEnum.BLANK, "camel case");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "Camel Case");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "CAMEL CASE");
    }

    @Test
    public void testDashCaseConversion() {
        // 测试输入: camel-case
        String input = "camel-case";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCase");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCase");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE");
        assertConversion(input, CaseModelEnum.DASH, "camel-case");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "CAMEL-CASE");
        assertConversion(input, CaseModelEnum.BLANK, "camel case");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "Camel Case");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "CAMEL CASE");
    }

    @Test
    public void testSpaceCaseConversion() {
        // 测试输入: camel case
        String input = "camel case";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCase");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCase");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE");
        assertConversion(input, CaseModelEnum.DASH, "camel-case");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "CAMEL-CASE");
        assertConversion(input, CaseModelEnum.BLANK, "camel case");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "Camel Case");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "CAMEL CASE");
    }

    // =======================================
    // 特殊情况测试
    // =======================================

    @Test
    public void testEmptyStringConversion() {
        // 测试输入: 空字符串
        String input = "";
        
        assertConversion(input, CaseModelEnum.CAMEL, "");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "");
        assertConversion(input, CaseModelEnum.SNAKE, "");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "");
        assertConversion(input, CaseModelEnum.DASH, "");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "");
        assertConversion(input, CaseModelEnum.BLANK, "");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "");
    }

    @Test
    public void testSingleCharacterConversion() {
        // 测试输入: 单个字符
        String input = "a";
        
        assertConversion(input, CaseModelEnum.CAMEL, "a");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "A");
        assertConversion(input, CaseModelEnum.SNAKE, "a");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "A");
        assertConversion(input, CaseModelEnum.DASH, "a");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "A");
        assertConversion(input, CaseModelEnum.BLANK, "a");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "A");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "A");
    }

    @Test
    public void testAllUpperCaseConversion() {
        // 测试输入: 全大写字符串
        String input = "ABC";
        
        assertConversion(input, CaseModelEnum.CAMEL, "abc");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "Abc");
        assertConversion(input, CaseModelEnum.SNAKE, "abc");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "ABC");
        assertConversion(input, CaseModelEnum.DASH, "abc");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "ABC");
        assertConversion(input, CaseModelEnum.BLANK, "abc");
        assertConversion(input, CaseModelEnum.BLANK_UPPER, "Abc");
        assertConversion(input, CaseModelEnum.BLANK_ALL_UPPER, "ABC");
    }

    // =======================================
    // 数字相关测试
    // =======================================

    @Test
    public void testNumbersInMiddleConversion() {
        // 测试输入: 中间带数字
        String input = "user_id_123";
        
        assertConversion(input, CaseModelEnum.CAMEL, "userId123");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "UserId123");
        assertConversion(input, CaseModelEnum.SNAKE, "user_id_123");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER_ID_123");
    }

    @Test
    public void testNumbersAtEndConversion() {
        // 测试输入: 数字结尾
        String input = "UserId123";

        assertConversion(input, CaseModelEnum.CAMEL, "userId123");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "UserId123");
        assertConversion(input, CaseModelEnum.SNAKE, "user_id_123");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER_ID_123");
    }

    @Test
    public void testNumbersAtBeginningConversion() {
        // 测试输入: 数字开头
        String input = "123user";
        
        assertConversion(input, CaseModelEnum.CAMEL, "123user");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "123user");
        assertConversion(input, CaseModelEnum.SNAKE, "123user");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "123USER");
    }

    @Test
    public void testNumbersWithUnderscoresConversion() {
        // 测试输入: 数字和下划线组合
        String input = "user_123_name";
        
        assertConversion(input, CaseModelEnum.CAMEL, "user123Name");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "User123Name");
        assertConversion(input, CaseModelEnum.SNAKE, "user_123_name");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER_123_NAME");
    }

    // =======================================
    // 连续大写字母测试
    // =======================================

    @Test
    public void testXmlHttpRequestConversion() {
        // 测试输入: XMLHttpRequest
        String input = "XMLHttpRequest";
        
        assertConversion(input, CaseModelEnum.CAMEL, "xmlHttpRequest");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "XmlHttpRequest");
        assertConversion(input, CaseModelEnum.SNAKE, "xml_http_request");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "XML_HTTP_REQUEST");
    }

    @Test
    public void testJsonParserConversion() {
        // 测试输入: JSONParser
        String input = "JSONParser";
        
        assertConversion(input, CaseModelEnum.CAMEL, "jsonParser");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "JsonParser");
        assertConversion(input, CaseModelEnum.SNAKE, "json_parser");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "JSON_PARSER");
    }

    @Test
    public void testHtmlDocumentConversion() {
        // 测试输入: HTMLDocument
        String input = "HTMLDocument";
        
        assertConversion(input, CaseModelEnum.CAMEL, "htmlDocument");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "HtmlDocument");
        assertConversion(input, CaseModelEnum.SNAKE, "html_document");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "HTML_DOCUMENT");
    }

    // =======================================
    // 混合格式测试
    // =======================================

    @Test
    public void testMixedCaseConversion() {
        // 测试输入: 混合格式
        String input = "camel_Case_Mixed";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCaseMixed");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCaseMixed");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case_mixed");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE_MIXED");
    }

    @Test
    public void testCamelDashMixedConversion() {
        // 测试输入: 驼峰和短横线混合
        String input = "Camel-Case-Mixed";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCaseMixed");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCaseMixed");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case_mixed");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE_MIXED");
    }

    @Test
    public void testUpperCaseMixedConversion() {
        // 测试输入: 大写和下划线混合
        String input = "CAMEL_case_MIXED";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCaseMixed");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCaseMixed");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case_mixed");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE_MIXED");
    }

    // =======================================
    // 类型检测测试
    // =======================================

    @Test
    public void testTypeDetection() {
        // 测试类型检测功能
        ICaseModel camelConverter = CaseModelEnum.CAMEL.getConvert();
        ICaseModel snakeConverter = CaseModelEnum.SNAKE.getConvert();
        ICaseModel dashConverter = CaseModelEnum.DASH.getConvert();
        ICaseModel blankConverter = CaseModelEnum.BLANK.getConvert();
        
        // 检测camelCase
        assertTrue(camelConverter.isThisType("camelCase"));
        // 检测snake_case
        assertTrue(snakeConverter.isThisType("snake_case"));
        // 检测kebab-case
        assertTrue(dashConverter.isThisType("kebab-case"));
        // 检测space case
        assertTrue(blankConverter.isThisType("space case"));
    }
    
    // =======================================
    // 新增测试场景：大写分隔符命名转换
    // =======================================
    
    @Test
    public void testUpperCaseSnakeCaseConversion() {
        // 测试输入: CAMEL_CASE
        String input = "CAMEL_CASE";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCase");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCase");
        assertConversion(input, CaseModelEnum.SNAKE, "camel_case");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "CAMEL_CASE");
    }
    
    @Test
    public void testUpperCaseDashCaseConversion() {
        // 测试输入: CAMEL-CASE
        String input = "CAMEL-CASE";
        
        assertConversion(input, CaseModelEnum.CAMEL, "camelCase");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "CamelCase");
        assertConversion(input, CaseModelEnum.DASH, "camel-case");
        assertConversion(input, CaseModelEnum.DASH_UPPER, "CAMEL-CASE");
    }
    
    // =======================================
    // 新增测试场景：数字位置场景
    // =======================================
    
    @Test
    public void testNumbersAtEndConversionMore() {
        // 测试输入: user123
        String input = "user123";
        
        assertConversion(input, CaseModelEnum.CAMEL, "user123");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "User123");
        assertConversion(input, CaseModelEnum.SNAKE, "user_123");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER_123");
    }
    
    // =======================================
    // 新增测试场景：混合大小写缩写
    // =======================================
    
    @Test
    public void testMixedCaseAbbreviationConversion() {
        // 测试输入: userID
        String input = "userID";
        
        assertConversion(input, CaseModelEnum.CAMEL, "userId");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "UserId");
        assertConversion(input, CaseModelEnum.SNAKE, "user_id");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER_ID");
    }
    
    @Test
    public void testInternalAbbreviationConversion() {
        // 测试输入: httpURLRequest
        String input = "httpURLRequest";
        
        assertConversion(input, CaseModelEnum.CAMEL, "httpUrlRequest");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "HttpUrlRequest");
        assertConversion(input, CaseModelEnum.SNAKE, "http_url_request");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "HTTP_URL_REQUEST");
    }
    
    // =======================================
    // 新增测试场景：特殊长度字符串
    // =======================================
    
    @Test
    public void testTwoCharacterUpperCaseConversion() {
        // 测试输入: AB
        String input = "AB";
        
        assertConversion(input, CaseModelEnum.CAMEL, "ab");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "Ab");
        assertConversion(input, CaseModelEnum.SNAKE, "ab");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "AB");
    }
    
    // =======================================
    // 新增测试场景：分隔符异常情况
    // =======================================
    
    @Test
    public void testLeadingTrailingSeparatorConversion() {
        // 测试输入: _user (前导下划线)
        String input1 = "_user";
        assertConversion(input1, CaseModelEnum.CAMEL, "user");
        
        // 测试输入: user_ (尾随下划线)
        String input2 = "user_";
        assertConversion(input2, CaseModelEnum.CAMEL, "user");
        
        // 测试输入: -user (前导短横线)
        String input3 = "-user";
        assertConversion(input3, CaseModelEnum.CAMEL, "user");
        
        // 测试输入: user- (尾随短横线)
        String input4 = "user-";
        assertConversion(input4, CaseModelEnum.CAMEL, "user");
    }
    
    @Test
    public void testConsecutiveSeparatorsConversion() {
        // 测试输入: user__id (连续下划线)
        String input1 = "user__id";
        assertConversion(input1, CaseModelEnum.CAMEL, "userId");
        
        // 测试输入: user--id (连续短横线)
        String input2 = "user--id";
        assertConversion(input2, CaseModelEnum.CAMEL, "userId");
        
        // 测试输入: user  id (连续空格)
        String input3 = "user  id";
        assertConversion(input3, CaseModelEnum.CAMEL, "userId");
    }
    
    // =======================================
    // 新增测试场景：全小写多字符字符串
    // =======================================
    
    @Test
    public void testAllLowerCaseConversion() {
        // 测试输入: user
        String input = "user";
        
        assertConversion(input, CaseModelEnum.CAMEL, "user");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "User");
        assertConversion(input, CaseModelEnum.SNAKE, "user");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER");
    }
    
    // =======================================
    // 新增测试场景：纯数字字符串
    // =======================================
    
    @Test
    public void testPureNumbersConversion() {
        // 测试输入: 123
        String input = "123";
        
        assertConversion(input, CaseModelEnum.CAMEL, "123");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "123");
        assertConversion(input, CaseModelEnum.SNAKE, "123");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "123");
    }
    
    // =======================================
    // 新增测试场景：复杂混合场景
    // =======================================
    
    @Test
    public void testComplexMixedConversion() {
        // 测试输入: USER123_NAME-ID
        String input = "USER123_NAME-ID";
        
        assertConversion(input, CaseModelEnum.CAMEL, "user123NameId");
        assertConversion(input, CaseModelEnum.CAMEL_UPPER, "User123NameId");
        assertConversion(input, CaseModelEnum.SNAKE, "user_123_name_id");
        assertConversion(input, CaseModelEnum.SNAKE_UPPER, "USER_123_NAME_ID");
    }
}
