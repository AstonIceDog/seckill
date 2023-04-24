package com.ly.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: some description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {
    private long code;
    private String message;
    private Object obj;

    /**
     * @description: 成功返回结果
     * @param: []
     * @return: com.ly.seckill.utils.vo.RespBean
     **/
    public static RespBean success(){
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }

    /**
     * @description: 成功返回结果
     * @param: [obj]
     * @return: com.ly.seckill.utils.vo.RespBean
     **/
    public static RespBean success(Object obj){
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), obj);
    }

    /**
     * @description: 失败返回结果
     * @param: [respBeanEnum]
     * @return: com.ly.seckill.utils.vo.RespBean
     **/
    public static RespBean error(RespBeanEnum respBeanEnum){
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }

    /**
     * @description: 失败返回结果
     * @param: [respBeanEnum, obj]
     * @return: com.ly.seckill.utils.vo.RespBean
     **/
    public static RespBean error(RespBeanEnum respBeanEnum, Object obj){
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), obj);
    }
}
