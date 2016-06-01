package com.vipheyue.livegame.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by heyue on 16/5/13.
 */
public class GameBean extends BmobObject {
    private Integer TotalIn_dong=0;//总下注
    private Integer TotalIn_nan=0;
    private Integer TotalIn_xi=0;
    private Integer TotalIn_bei=0;
    private Boolean  finish=false;
    private Integer  answer;

    public Integer getAnswer() {
        return answer;
    }

    public void setAnswer(Integer answer) {
        this.answer = answer;
    }

    public Boolean getFinish() {
        return finish;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public Integer getTotalIn_dong() {
        return TotalIn_dong;
    }

    public void setTotalIn_dong(Integer totalIn_dong) {
        TotalIn_dong = totalIn_dong;
    }

    public Integer getTotalIn_nan() {
        return TotalIn_nan;
    }

    public void setTotalIn_nan(Integer totalIn_nan) {
        TotalIn_nan = totalIn_nan;
    }

    public Integer getTotalIn_xi() {
        return TotalIn_xi;
    }

    public void setTotalIn_xi(Integer totalIn_xi) {
        TotalIn_xi = totalIn_xi;
    }

    public Integer getTotalIn_bei() {
        return TotalIn_bei;
    }

    public void setTotalIn_bei(Integer totalIn_bei) {
        TotalIn_bei = totalIn_bei;
    }
    public Integer getTotal() {
        return TotalIn_dong+TotalIn_nan+TotalIn_xi+TotalIn_bei;
    }
}
