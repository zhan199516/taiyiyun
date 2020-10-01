package com.taiyiyun.passport.sqlserver.po;

import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by nina on 2017/10/20.
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
//        IPictureTempDao dao = ctx.getBean(IPictureTempDao.class);
//        PictureTemp pt = dao.getByUUID("f741d27fe4774f29a02584a2ce110553");
//        System.out.println(pt.getFileName());
//        System.out.println(pt.getCreationTime());
//        System.out.println(pt.getFinished());
//        System.out.println(pt.getUUID());
//        System.out.println(pt.getPTypeID());
//        IEntityDao entityDao = ctx.getBean(IEntityDao.class);
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("idCard", "450204198509111421");
//        params.put("uuid", "06140513d8974c148e0bfb7ff907b676");
//        List<Entity> entities = entityDao.selectEntitiesByIDCardAndUUID(params);
//        System.out.println(entities.size());
//        Entity entity = entities.get(0);
//        System.out.println(entity.getEntityUnqueId());
//        System.out.println(entity.getTypeId());
//        IDeveloperDao developerDao = ctx.getBean(IDeveloperDao.class);
//        Developer dev = developerDao.selectDeveloperByAppKey("093EEE3838C14549920EE33A6068C9D2");
//        System.out.println(dev.getAppId());
//        System.out.println(dev.getAppName());
        System.out.println("------------------------------------");
        IGroupMemberDao gmDao = ctx.getBean(IGroupMemberDao.class);
        System.out.println(gmDao.selectGroupMemberCounts("0c217f6c17b444b7ac5fb2a51b5097f6"));
    }
}
