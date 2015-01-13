/**
 * @Title: WarehouseService.java 
 * @Package com.glacier.frame.service.basicdatas 
 * @author junjie.zheng  
 * @email 1203807137@qq.com
 * @date 2015-1-13 下午2:19:54 
 * @company (开发公司)    珠海市冰川软件有限公司
 * @copyright (版权)    本文件归属珠海市冰川软件有限公司所有
 * @version V1.0
 * @modify (修改) 
 *        	<p>
				第一次修改：
				时间：2015-1-13
				修改人：xichao.dong 
				修改内容简介 ：
			</p>              
 * @Review (审核人) ：xichao.dong
 * 
 */
package com.glacier.frame.service.basicdatas;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.glacier.basic.util.RandomGUID;
import com.glacier.frame.dao.basicdatas.WarehouseMapper;
import com.glacier.frame.dto.query.basicdatas.WarehouseQueryDTO;
import com.glacier.frame.entity.basicdatas.Warehouse;
import com.glacier.frame.entity.basicdatas.WarehouseExample;
import com.glacier.frame.entity.basicdatas.WarehouseExample.Criteria;
import com.glacier.frame.entity.system.User;
import com.glacier.frame.util.MethodLog;
import com.glacier.jqueryui.util.JqGridReturn;
import com.glacier.jqueryui.util.JqPager;
import com.glacier.jqueryui.util.JqReturnJson;

/**
 * @ClassName: WarehouseService 
 * @Description: TODO(仓库信息业务层) 
 * @author junjie.zheng
 * @email 1203807137@qq.com
 * @date 2015-1-13 下午2:19:54
 */
@Service
@Transactional(readOnly=true,propagation = Propagation.REQUIRED)
public class WarehouseService {
	@Autowired
	private WarehouseMapper warehouseMapper;
	
	/**
	 * @Title: listAsGrid
	 * @Description: TODO(获取所有仓库信息)
	 * @param @param pager
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @throws
	 */
	public Object listAsGrid(JqPager jqPager,WarehouseQueryDTO warehouseQueryDTO) {
		JqGridReturn returnResult = new JqGridReturn();
		WarehouseExample warehouseExample = new WarehouseExample();
		Criteria queryCriteria = warehouseExample.createCriteria();
		warehouseQueryDTO.setQueryCondition(queryCriteria);
		if (null != jqPager.getPage() && null != jqPager.getRows()) {// 设置排序信息
			warehouseExample.setLimitStart((jqPager.getPage() - 1)* jqPager.getRows());
			warehouseExample.setLimitEnd(jqPager.getRows());
		}
		if (StringUtils.isNotBlank(jqPager.getSort())&& StringUtils.isNotBlank(jqPager.getOrder())) {// 设置排序信息
			warehouseExample.setOrderByClause(jqPager.getOrderBy("temp_warehouse_"));
		}
		List<Warehouse> warehouse = warehouseMapper.selectByExample(warehouseExample); // 查询所有仓库信息列表
		int total = warehouseMapper.countByExample(warehouseExample); // 查询总页数
		returnResult.setRows(warehouse);
		returnResult.setTotal(total);
		return returnResult;// 返回ExtGrid表
	}

	/**
	 * @Title: getWarehouse
	 * @Description: TODO(获取仓库信息对象)
	 * @param @param goodsId
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @throws
	 */
	public Object getWarehouse(String warehouseId) {
		Warehouse warehouse = warehouseMapper.selectByPrimaryKey(warehouseId);
		return warehouse;
	}
	
	/**
	 * @Title: addWarehouse
	 * @Description: TODO(新增仓库信息)
	 * @param @param warehouse
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @throws
	 */
	@Transactional(readOnly = false)
	@MethodLog(opera = "Warehouse_add")
	public Object addWarehouse(Warehouse warehouse) {
		Subject pricipalSubject = SecurityUtils.getSubject();
		User pricipalUser = (User) pricipalSubject.getPrincipal();
		JqReturnJson returnResult = new JqReturnJson();// 构建返回结果，默认结果为false
		WarehouseExample warehouseExample = new WarehouseExample();
		int count = 0;
		// 防止仓库名称重复
		warehouseExample.createCriteria().andWarehouseNameEqualTo(warehouse.getWarehouseName());
		count = warehouseMapper.countByExample(warehouseExample);
		if (count > 0) {
			returnResult.setMsg("仓库名称信息重复,请重新填写!");
			returnResult.setSuccess(false);
			return returnResult;
		}
		warehouse.setWarehouseId(RandomGUID.getRandomGUID());
		warehouse.setCreater(pricipalUser.getUserCnName());
		warehouse.setCreateTime(new Date());
		warehouse.setUpdater(pricipalUser.getUserCnName());
		warehouse.setUpdateTime(new Date());
		count = warehouseMapper.insert(warehouse);
		if (count == 1) {
			returnResult.setSuccess(true);
			returnResult.setMsg("【" + warehouse.getWarehouseName()+ "】仓库信息已保存");
		} else {
			returnResult.setMsg("发生未知错误，仓库信息保存失败");
		}
		return returnResult;
	}

	/**
	 * @Title: editWarehouse
	 * @Description: TODO(修改仓库信息)
	 * @param @param warehouse
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @throws
	 */
	@Transactional(readOnly = false)
	@MethodLog(opera = "Warehouse_edit")
	public Object editWarehouse(Warehouse warehouse) {
		Subject pricipalSubject = SecurityUtils.getSubject();
		User pricipalUser = (User) pricipalSubject.getPrincipal();
		JqReturnJson returnResult = new JqReturnJson();// 构建返回结果，默认结果为false
		int count = 0;
		// 防止仓库名称重复
		WarehouseExample warehouseExample = new WarehouseExample();
		warehouseExample.createCriteria().andWarehouseNameEqualTo(warehouse.getWarehouseName()).andWarehouseIdNotEqualTo(warehouse.getWarehouseId());
		count = warehouseMapper.countByExample(warehouseExample);
		if (count > 0) {
			returnResult.setMsg("仓库名称信息重复,请重新填写!");
			returnResult.setSuccess(false);
			return returnResult;
		}
		warehouse.setCreater(pricipalUser.getUserCnName());
		warehouse.setCreateTime(new Date());
		warehouse.setUpdater(pricipalUser.getUserCnName());
		warehouse.setUpdateTime(new Date());
		count = warehouseMapper.updateByPrimaryKeySelective(warehouse);
		if (count == 1) {
			returnResult.setSuccess(true);
			returnResult.setMsg("【" + warehouse.getWarehouseName()+ "】仓库信息已保存");
		} else {
			returnResult.setMsg("发生未知错误，仓库保存失败");
		}
		return returnResult;
	}

	/**
	 * @Title: delWarehouse
	 * @Description: TODO(删除仓库信息)
	 * @param @param warehouseIds
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @throws
	 */
	@Transactional(readOnly = false)
	@MethodLog(opera = "Warehouse_del")
	public Object delWarehouse(List<String> warehouseIds) {
		JqReturnJson returnResult = new JqReturnJson();// 构建返回结果，默认结果为false
		int count = 0;
		if (warehouseIds.size() > 0) {
			WarehouseExample warehouseExample = new WarehouseExample();
			warehouseExample.createCriteria().andWarehouseIdIn(warehouseIds);
			count = warehouseMapper.deleteByExample(warehouseExample);
			if (count > 0) {
				returnResult.setSuccess(true);
				returnResult.setMsg("成功删除了【 " + count+ " 】条仓库信息");
			} else {
				returnResult.setMsg("发生未知错误，仓库信息删除失败");
			}
		}
		return returnResult;
	}
}