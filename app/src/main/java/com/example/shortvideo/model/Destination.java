package com.example.shortvideo.model;

public class Destination {

    /**
     * isFragment : true
     * asStarter : false
     * needLogin : false
     * pageUrl : main/tabs/dash
     * id : 508098738
     * ClazName : com.example.shortvideo.ui.dashboard.DashboardFragment
     */

    private boolean isFragment;
    private boolean asStarter;
    private boolean needLogin;
    private String pageUrl;
    private int id;
    private String ClazName;

    public boolean isIsFragment() {
        return isFragment;
    }

    public void setIsFragment(boolean isFragment) {
        this.isFragment = isFragment;
    }

    public boolean isAsStarter() {
        return asStarter;
    }

    public void setAsStarter(boolean asStarter) {
        this.asStarter = asStarter;
    }

    public boolean isNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClazName() {
        return ClazName;
    }

    public void setClazName(String ClazName) {
        this.ClazName = ClazName;
    }
}
