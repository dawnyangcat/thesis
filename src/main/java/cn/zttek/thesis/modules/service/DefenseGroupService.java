package cn.zttek.thesis.modules.service;

import cn.zttek.thesis.common.base.BaseService;
import cn.zttek.thesis.common.utils.JsonUtils;
import cn.zttek.thesis.modules.enums.DefenseStatus;
import cn.zttek.thesis.modules.enums.TitleLevel;
import cn.zttek.thesis.modules.expand.ThesisDefenseStudent;
import cn.zttek.thesis.modules.expand.ThesisDefenseTeacher;
import cn.zttek.thesis.modules.expand.ThesisResult;
import cn.zttek.thesis.modules.mapper.DefenseGroupMapper;
import cn.zttek.thesis.modules.mapper.DefenseTaskMapper;
import cn.zttek.thesis.modules.model.DefenseGroup;
import cn.zttek.thesis.modules.model.DefenseTask;
import cn.zttek.thesis.modules.model.Thesis;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Mankind on 2017/8/20.
 */
@Service
public class DefenseGroupService extends BaseService<DefenseGroup>{

    @Autowired
    private DefenseGroupMapper defenseGroupMapper;

    @Autowired
    private DefenseTaskMapper defenseTaskMapper;

    public PageInfo<DefenseGroup> listAll(Integer page, Integer rows, Long taskid) throws Exception{
        log.info("===查询答辩任务下所有答辩小组列表===");
        PageHelper.startPage(page, rows);
        List<DefenseGroup> list =defenseGroupMapper.selectByTask(taskid);
        PageInfo<DefenseGroup> pageInfo = new PageInfo<DefenseGroup>(list);
        return pageInfo;
    }

    public void deleteById(Long id) throws  Exception{
        log.info("===删除某个答辩小组[" + id + "]===");
        DefenseGroup defenseGroup=this.queryById(id);
        defenseGroupMapper.deleteByPrimaryKey(id);
    }

    public void deleteStudentByJSON(String jsondata,DefenseGroup defenseGroup) throws Exception{
        log.info("===删除答辩小组下的参与学生====");
        if(defenseGroup.getStudents()!=null){
            List<ThesisDefenseStudent> stulist= JsonUtils.jsonToList(defenseGroup.getStudents(),ThesisDefenseStudent.class);
            List<ThesisDefenseStudent> deleteStuList=JsonUtils.jsonToList(jsondata,ThesisDefenseStudent.class);
            log.info("stulist数据:"+stulist);
            log.info("deleteStuList数据:"+deleteStuList);
            log.info("删除前长度："+stulist.size());
            stulist.removeAll(deleteStuList);
            log.info("删除后长度："+stulist.size());
            defenseGroup.setStudents(JsonUtils.objectToJson(stulist));
            defenseGroupMapper.updateByPrimaryKey(defenseGroup);
        }
    }
    public void deleteTeacherByJSON(String jsondata,DefenseGroup defenseGroup) throws Exception{
        log.info("===删除答辩小组下的参与教师====");
        if(defenseGroup.getTeachers()!=null){
            List<ThesisDefenseTeacher> teacherlist=JsonUtils.jsonToList(defenseGroup.getTeachers(),ThesisDefenseTeacher.class);
            List<ThesisDefenseTeacher> deleteTeacherList=JsonUtils.jsonToList(jsondata,ThesisDefenseTeacher.class);
            teacherlist.removeAll(deleteTeacherList);
            defenseGroup.setTeachers(JsonUtils.objectToJson(teacherlist));
            defenseGroupMapper.updateByPrimaryKey(defenseGroup);
        }
    }
    public void addStudentByJSON(String jsondata,DefenseGroup defenseGroup) throws Exception{
        log.info("===添加答辩小组下的参与学生====");
        List<ThesisDefenseStudent> stulist=new ArrayList<ThesisDefenseStudent>();
        if(defenseGroup.getStudents()!=null) {
            stulist = JsonUtils.jsonToList(defenseGroup.getStudents(), ThesisDefenseStudent.class);
        }
        List<ThesisDefenseStudent> addStuList=JsonUtils.jsonToList(jsondata,ThesisDefenseStudent.class);
        stulist.addAll(addStuList);
        defenseGroup.setStudents(JsonUtils.objectToJson(stulist));
        defenseGroupMapper.updateByPrimaryKey(defenseGroup);
    }
    public void addTeacherByJSON(String jsondata,DefenseGroup defenseGroup) throws Exception{
        log.info("===添加答辩任务下的参与教师====");
        List<ThesisDefenseTeacher> teacherlist=new ArrayList<ThesisDefenseTeacher>();
        if(defenseGroup.getTeachers()!=null){
            teacherlist=JsonUtils.jsonToList(defenseGroup.getTeachers(),ThesisDefenseTeacher.class);
        }
        List<ThesisDefenseTeacher> addTeacherList=JsonUtils.jsonToList(jsondata,ThesisDefenseTeacher.class);
        teacherlist.addAll(addTeacherList);
        defenseGroup.setTeachers(JsonUtils.objectToJson(teacherlist));
        defenseGroupMapper.updateByPrimaryKey(defenseGroup);
    }
    public PageInfo<ThesisDefenseStudent> listStudent( Long taskid, String stuno, DefenseStatus defenseStatus,DefenseGroup defenseGroup) throws Exception {
        log.info("===查询答辩任务下所有未分配的学生===");
        if(taskid==null)taskid=defenseGroup.getTaskid();
        DefenseTask task=defenseTaskMapper.selectByPrimaryKey(taskid);
        List<ThesisDefenseStudent> studentlist=new ArrayList<ThesisDefenseStudent>();
        if(task.getStudents()!=null){
            //获取答辩任务下所有学生列表
            studentlist=JsonUtils.jsonToList(task.getStudents(),ThesisDefenseStudent.class);
            //过滤出对应答辩类型的学生
            if(defenseStatus!=null){
                int length=studentlist.size();
                for(int i=length-1;i>=0;i--){
                    if(!studentlist.get(i).getDefenseStatus().equals(defenseStatus)){
                        studentlist.remove(i);
                    }
                }
            }
            //获取已经分配的所有学生
            List<DefenseGroup> groups=defenseGroupMapper.selectByTask(taskid);
            for(DefenseGroup group:groups){
                if(group.getStudents()!=null){
                    List<ThesisDefenseStudent> groupStudentList=JsonUtils.jsonToList(group.getStudents(),ThesisDefenseStudent.class);
                    studentlist.removeAll(groupStudentList);
                }
            }
            Collections.sort(studentlist);
        }
        PageInfo<ThesisDefenseStudent> pageInfo=new PageInfo<ThesisDefenseStudent>(studentlist);
        return pageInfo;
    }

    public PageInfo<ThesisDefenseTeacher> listTeacher(Long taskid,String leaderJSON,String secretaryJSON,TitleLevel titleLevel,DefenseGroup defenseGroup) throws Exception {
        log.info("===查询答辩任务下所有未分配的教师===");
        if(taskid==null)taskid=defenseGroup.getTaskid();
        DefenseTask task=defenseTaskMapper.selectByPrimaryKey(taskid);
        List<ThesisDefenseTeacher> teacherlist=new ArrayList<ThesisDefenseTeacher>();
        if(task.getTeachers()!=null){
            //获取答辩任务下所有教师列表
            teacherlist=JsonUtils.jsonToList(task.getTeachers(),ThesisDefenseTeacher.class);
            //过滤不符合等级条件的教师
            teacherlist=levelFilter(titleLevel,teacherlist);
            //过滤已经分配的教师
            teacherlist=existTeacherFilter(teacherlist);
            if(defenseGroup.getId()!=null&&defenseGroup.getId()>0){
                teacherlist.add(defenseGroupMapper.selectTeacherByUserId(defenseGroup.getLeaderid()));
                teacherlist.add(defenseGroupMapper.selectTeacherByUserId(defenseGroup.getSecretaryid()));
            }
            //过滤前面被选择的答辩组长
            if(StringUtil.isNotEmpty(leaderJSON)){
                teacherlist.remove(JsonUtils.jsonToPojo(leaderJSON,ThesisDefenseTeacher.class));
            }
            //过滤前面被选择的答辩秘书
            if(StringUtil.isNotEmpty(secretaryJSON)){
                teacherlist.remove((JsonUtils.jsonToPojo(secretaryJSON,ThesisDefenseTeacher.class)));
            }
            Collections.sort(teacherlist);
        }
        PageInfo<ThesisDefenseTeacher> pageInfo=new PageInfo<ThesisDefenseTeacher>(teacherlist);
        return pageInfo;
    }
    public List<ThesisDefenseTeacher> levelFilter(TitleLevel titleLevel,List<ThesisDefenseTeacher> teacherlist) throws  Exception{
        //过滤不符合等级条件的教师
        if(titleLevel!=null){
            int length=teacherlist.size();
            for(int i=length-1;i>=0;i--){
                if(!teacherlist.get(i).getTitleLevel().equals(titleLevel)){
                    teacherlist.remove(i);
                }
            }
        }
        return teacherlist;
    }
    public String getTeacherJSON(Long teacherid) throws Exception{
        log.info("转化后的json"+JsonUtils.objectToJson(defenseGroupMapper.selectTeacherByUserId(teacherid)));
        return JsonUtils.objectToJson(defenseGroupMapper.selectTeacherByUserId(teacherid));
    }
    public List<ThesisDefenseTeacher> existTeacherFilter(List<ThesisDefenseTeacher> teacherlist) throws  Exception{
        List<DefenseGroup> groups=defenseGroupMapper.selectAll();
        for(DefenseGroup group:groups){
            //去除答辩参加教师
            if(group.getTeachers()!=null){
                List<ThesisDefenseTeacher> groupTeacherList=JsonUtils.jsonToList(group.getTeachers(),ThesisDefenseTeacher.class);
                teacherlist.removeAll(groupTeacherList);
            }
            //去除答辩秘书和答辩组长
            if(group.getSecretaryid()!=null){
                ThesisDefenseTeacher leader=defenseGroupMapper.selectTeacherByUserId(group.getLeaderid());
                teacherlist.remove(leader);
            }
            if(group.getSecretaryid()!=null){
                ThesisDefenseTeacher secretary=defenseGroupMapper.selectTeacherByUserId(group.getSecretaryid());
                teacherlist.remove(secretary);
            }
        }
        return teacherlist;
    }
    public Integer getGroupno(Long taskid) throws Exception{
        List<DefenseGroup> list=defenseGroupMapper.selectByTask(taskid);
        DefenseTask defenseTask=defenseTaskMapper.selectByPrimaryKey(taskid);
        int[] table=new int[defenseTask.getNums()+1];
        for(int i=0;i<list.size();i++){
            table[list.get(i).getGroupno()]=1;
        }
        int index=1;
        for(int i=defenseTask.getNums();i>0;i--){
            if(table[i]!=1)index=i;
        }
        return index;
    }
}
