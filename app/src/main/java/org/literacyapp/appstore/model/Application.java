package org.literacyapp.appstore.model;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.literacyapp.appstore.dao.converter.LocaleConverter;
import org.literacyapp.appstore.dao.converter.StringSetConverter;
import org.literacyapp.model.enums.Locale;
import org.literacyapp.model.enums.content.LiteracySkill;

import java.util.Set;

@Entity
public class Application {

    @Id
    private Long id;

    @NotNull
    @Convert(converter = LocaleConverter.class, columnType = String.class)
    private Locale locale;

    @NotNull
    private String packageName;

    @Convert(converter = StringSetConverter.class, columnType = String.class)
    private Set<LiteracySkill> literacySkills;

//    numeracySkills
//
//    applicationStatus

    @Generated(hash = 460732977)
    public Application(Long id, @NotNull Locale locale, @NotNull String packageName,
            Set<LiteracySkill> literacySkills) {
        this.id = id;
        this.locale = locale;
        this.packageName = packageName;
        this.literacySkills = literacySkills;
    }

    @Generated(hash = 312658882)
    public Application() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Set<LiteracySkill> getLiteracySkills() {
        return literacySkills;
    }

    public void setLiteracySkills(Set<LiteracySkill> literacySkills) {
        this.literacySkills = literacySkills;
    }
}
