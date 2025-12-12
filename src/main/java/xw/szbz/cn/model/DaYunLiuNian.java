package xw.szbz.cn.model;

import java.util.List;

/**
 * 大运及其对应的流年
 * 每个大运包含其完整名称和该大运时期的所有流年
 */
public class DaYunLiuNian {
    private String daYunFullName;      // 大运的完整名称（如"甲子"）
    private List<String> liuNianList;  // 该大运对应的流年列表（干支字符串）

    public DaYunLiuNian() {
    }

    public DaYunLiuNian(String daYunFullName, List<String> liuNianList) {
        this.daYunFullName = daYunFullName;
        this.liuNianList = liuNianList;
    }

    public String getDaYunFullName() {
        return daYunFullName;
    }

    public void setDaYunFullName(String daYunFullName) {
        this.daYunFullName = daYunFullName;
    }

    public List<String> getLiuNianList() {
        return liuNianList;
    }

    public void setLiuNianList(List<String> liuNianList) {
        this.liuNianList = liuNianList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"daYunFullName\":\"").append(daYunFullName).append("\",");
        sb.append("\"liuNianList\":[");
        if (liuNianList != null && !liuNianList.isEmpty()) {
            for (int i = 0; i < liuNianList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(liuNianList.get(i)).append("\"");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
