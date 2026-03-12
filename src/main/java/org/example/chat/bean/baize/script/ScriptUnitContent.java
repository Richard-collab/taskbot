package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 主流程语料
 */
@Getter
@Setter
public class ScriptUnitContent implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;

    /**
     * 语句内容
     */
    private String content;

    /**
     * 是否播放,当语句被修改后前端会置false，需要重新验听
     */
    private Boolean isPlayed;

    /**
     * 语料内容id
     */
    private Long multiContentId;

    /**
     * 排序
     */
    private Integer orders;

    /**
     * 语句名称
     */
    private String contentName;

    /**
     * 话术id
     */
    private Long scriptId;

    /**
     * 语料id
     */
    private Long corpusId;

    /**
     * 音频路径
     */
    private String audioPath;

    /**
     * 上传状态
     */
    private String uploadStatus;

    /**
     * 修改状态
     */
    private String updateStatus;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * 删除标志位
     */
    private Boolean deleted;

    /**
     * 语料类型
     */
    private CorpusType corpusType;

    /**
     * 文本类型
     */
    private ContentType contentType;

    /**
     * 音频识别文本
     */
    private String asrTxt;

    /**
     * 音频识别结果
     */
    private Double asrResult;

    /**
     * 处理状态
     */
    private AsrStatus asrStatus;

    /**
     * 打断类型
     */
    private InterruptType interruptType;

    /**
     * 允许打断时间
     */
    private Integer allowedInterruptTime;

    /**
     * 打断垫句ID
     */
    private Long preInterruptCorpusId;

    /**
     * 续播垫句(打断)ID
     */
    private Long preContinueCorpusIdForInterrupt;

    /**
     * 续播垫句(返回)ID
     */
    private Long preContinueCorpusIdForReturn;

    /**
     * 续播类型
     */
    private CorpusReturnType corpusReturnType;

    /**
     * 承接语句ID
     */
    private Long preUndertakeCorpusId;

    /**
     * 打断语料
     */
    private List<Long> interruptCorpusIdsForEnd;

    /**
     * 验听标记文本
     */
    private String audioTag;

    /**
     * 是否播放,0待验听，1已验听，2已标记 重新上传和修改文字内容会改为0，其他状态修改时如果存在audioTag则均视为已标记状态  当语句被修改后前端会置false，需要重新验听
     */
    private String audioStatus;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
