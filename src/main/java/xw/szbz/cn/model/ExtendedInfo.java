package xw.szbz.cn.model;

import java.util.List;

/**
 * 扩展信息
 * 包含基本信息 + 大运 + 流年
 */
public class ExtendedInfo {
    private BasicInfo basicInfo;           // 基本信息
    private List<DaYun> daYunList;        // 大运列表
    private List<LiuNian> liuNianList;    // 流年列表
    private int qiYunAge;                 // 起运年龄（周岁，保留用于向后兼容）
    private QiYunInfo qiYunInfo;          // 精确起运信息（年、月、日、虚岁等）
    private boolean shunPai;              // 是否顺排（true=顺排，false=逆排）

    public ExtendedInfo() {
    }

    public BasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(BasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public List<DaYun> getDaYunList() {
        return daYunList;
    }

    public void setDaYunList(List<DaYun> daYunList) {
        this.daYunList = daYunList;
    }

    public List<LiuNian> getLiuNianList() {
        return liuNianList;
    }

    public void setLiuNianList(List<LiuNian> liuNianList) {
        this.liuNianList = liuNianList;
    }

    public int getQiYunAge() {
        return qiYunAge;
    }

    public void setQiYunAge(int qiYunAge) {
        this.qiYunAge = qiYunAge;
    }

    public QiYunInfo getQiYunInfo() {
        return qiYunInfo;
    }

    public void setQiYunInfo(QiYunInfo qiYunInfo) {
        this.qiYunInfo = qiYunInfo;
    }

    public boolean isShunPai() {
        return shunPai;
    }

    public void setShunPai(boolean shunPai) {
        this.shunPai = shunPai;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"basicInfo\":").append(basicInfo != null ? basicInfo.toString() : "null").append(",");
        sb.append("\"qiYunAge\":").append(qiYunAge).append(",");
        sb.append("\"qiYunInfo\":").append(qiYunInfo != null ? qiYunInfo.toString() : "null").append(",");
        sb.append("\"shunPai\":").append(shunPai).append(",");

        // 大运列表
        sb.append("\"daYunList\":[");
        if (daYunList != null && !daYunList.isEmpty()) {
            for (int i = 0; i < daYunList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(daYunList.get(i).toString());
            }
        }
        sb.append("],");

        // 流年列表
        sb.append("\"liuNianList\":[");
        if (liuNianList != null && !liuNianList.isEmpty()) {
            for (int i = 0; i < liuNianList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(liuNianList.get(i).toString());
            }
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }
}
