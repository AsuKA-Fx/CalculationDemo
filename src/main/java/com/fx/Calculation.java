package com.fx;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * ClassName: Calculation
 * Package: com.fx
 * Description:
 * Create: 2024/4/25 - 17:48
 *
 * @author : Fang-
 * @version : v1.0
 */
@Slf4j
public class Calculation {
    private enum PRIORITY {
        HIGHER,
        //        EQUAL,
        LOWER
    }

    private enum OPERATOR {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/");
        private final String operatorSymbol;
        private static final Map<String, OPERATOR> operatorSymbolMap = new HashMap<>();

        OPERATOR(String operatorSymbol) {
            this.operatorSymbol = operatorSymbol;
        }

        private String getOperatorSymbol() {
            return this.operatorSymbol;
        }

        static {
            for (OPERATOR operator : OPERATOR.values()) {
                operatorSymbolMap.put(operator.getOperatorSymbol(), operator);
            }
        }
    }

//    private static final String[] OPERATE_ARR = {"+", "-", "*", "/"};

    // TODO: 目前的顺序需要与OPERATOR枚举中对应顺序一致
    private static final PRIORITY[][] PRIORITY_MATRIX = {
            {PRIORITY.HIGHER, PRIORITY.HIGHER, PRIORITY.LOWER, PRIORITY.LOWER},
            {PRIORITY.HIGHER, PRIORITY.HIGHER, PRIORITY.LOWER, PRIORITY.LOWER},
            {PRIORITY.HIGHER, PRIORITY.HIGHER, PRIORITY.HIGHER, PRIORITY.HIGHER},
            {PRIORITY.HIGHER, PRIORITY.HIGHER, PRIORITY.HIGHER, PRIORITY.HIGHER}
    };

    private static final String BLANK_REG_STR = "\\s+";
    private static final String NUM_BEFORE_OPERATOR_REG_STR = "(\\d+)([+*/-])";
    private static final String OPERATOR_BEFORE_NUM_REG_STR = "([+*/-])(\\d+)";
    // TODO: 自定义其他的异常类
    private static final String EXP_INVALID_EXCEPTION_MESSAGE = "expression invalid";
    private static final String DIVIDE_BY_ZERO_EXCEPTION_MESSAGE = "/ by zero";

    // FIXME: 并发时是否会有异常
    public static int doCalculate(String expression) throws Exception {
        if (!isValid(expression)) {
            throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
        }

        log.debug("expression={}", expression);

        String[] splitExpressionArr = splitExp(expression);
        if (null == splitExpressionArr || expression.length() == 0) {
            throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
        }
        log.debug("splitExpressionArr={}", Arrays.toString(splitExpressionArr));

        CalculationStack<String> operateStack = new CalculationStack<>();
        CalculationStack<Integer> numericStack = new CalculationStack<>();

        for (String str : splitExpressionArr) {
            if (isNumeric(str)) { // 为数字
                numericStack.push(NumberUtils.createInteger(str));
            } else if (OPERATOR.operatorSymbolMap.containsKey(str)) { // 为运算符
                if (!operateStack.isEmpty()) {
                    PRIORITY priority = compareOperatorPriority(operateStack.peek(), str);
                    if (PRIORITY.HIGHER == priority) {
                        updateStackByCalculate(numericStack, operateStack);
                    }
                }
                operateStack.push(str);
            } else { // 为数字、运算符之外的情形
                throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
            }
        }

        while (!operateStack.isEmpty()) {
            updateStackByCalculate(numericStack, operateStack);
        }
        if (CollectionUtils.isEmpty(numericStack) || numericStack.size() != 1) {
            throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
        }

        return numericStack.pop();
    }

    public static boolean isValid(String expression) {
        // TODO: 实现表达式有效性校验，非数学表达式字符的入参校验
        return !StringUtils.isEmpty(expression);
    }

    /**
     * 将算数表达式根据数字或运算符分割
     */
    private static String[] splitExp(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return null;
        }

//        expression = expression.replaceAll(BLANK_REG_STR, "");
        expression = expression.replaceAll(NUM_BEFORE_OPERATOR_REG_STR, "$1 $2");
        expression = expression.replaceAll(OPERATOR_BEFORE_NUM_REG_STR, "$1 $2");

        return expression.split(BLANK_REG_STR);
    }

    // TODO: 目前只判断是否为整数
    private static boolean isNumeric(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        return NumberUtils.isParsable(str);
    }

    /**
     * 比较两个运算符的优先级
     */
    private static PRIORITY compareOperatorPriority(String operator1, String operator2) throws Exception {
        if (StringUtils.isEmpty(operator1) || StringUtils.isEmpty(operator2)) {
            throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
        }

        int index1 = OPERATOR.operatorSymbolMap.get(operator1).ordinal();
        int index2 = OPERATOR.operatorSymbolMap.get(operator2).ordinal();

        // TODO: 未处理ArrayIndexOutOfBoundsException
        PRIORITY priority = PRIORITY_MATRIX[index1][index2];

        log.debug("operator1={}, operator2={}, result of compareOperatorPriority is {}", operator1, operator2, priority);

        return priority;
    }

    /**
     * 计算，并更新numericStack、operateStack
     */
    private static void updateStackByCalculate(Stack<Integer> numericStack, Stack<String> operateStack) throws Exception {
        if (CollectionUtils.isEmpty(numericStack) || numericStack.size() < 2 || CollectionUtils.isEmpty(operateStack)) {
            throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
        }

        int num2 = numericStack.pop();
        int num1 = numericStack.pop();

        String operatorStr = operateStack.pop();
        OPERATOR operator = OPERATOR.operatorSymbolMap.get(operatorStr);
        if (null == operator) {
            throw new Exception(EXP_INVALID_EXCEPTION_MESSAGE);
        }

        log.debug("num1={}, num2={}, operator={}", num1, num2, operator.operatorSymbol);

        if (OPERATOR.ADD == operator) {
            numericStack.push(num1 + num2);
        } else if (OPERATOR.SUB == operator) {
            numericStack.push(num1 - num2);
        } else if (OPERATOR.MUL == operator) {
            numericStack.push(num1 * num2);
        } else if (OPERATOR.DIV == operator) {
            if (num2 == 0) {
                throw new ArithmeticException(DIVIDE_BY_ZERO_EXCEPTION_MESSAGE);
            }
            numericStack.push(num1 / num2);
        }
    }
}
