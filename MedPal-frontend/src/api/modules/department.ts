import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

// 科室信息
export interface Department {
  id: number;
  hospitalId: number;
  departmentName: string;
  departmentCode?: string;
  description?: string;
  status: number; // 0-禁用，1-启用
  createTime: string;
  updateTime: string;
}

/**
 * 获取科室列表
 * @param current 当前页码
 * @param size 每页大小
 * @param hospitalId 医院ID（可选）
 */
export function listDepartments(
  current: number = 1,
  size: number = 10,
  hospitalId?: number,
  keyword?: string
): Promise<BaseResponse<PageData<Department>>> {
  return request.get('/department/list', {
    params: { current, size, hospitalId, keyword },
  }) as Promise<BaseResponse<PageData<Department>>>;
}

/**
 * 获取科室详情
 * @param id 科室ID
 */
export function getDepartment(id: number): Promise<BaseResponse<Department>> {
  return request.get(`/department/${id}`) as Promise<BaseResponse<Department>>;
}

/**
 * 创建科室
 * @param department 科室信息
 */
export function createDepartment(department: Partial<Department>): Promise<BaseResponse<Department>> {
  return request.post('/department/create', department) as Promise<BaseResponse<Department>>;
}

/**
 * 更新科室信息
 * @param department 科室信息
 */
export function updateDepartment(department: Partial<Department> & { id: number }): Promise<BaseResponse<boolean>> {
  return request.post('/department/update', department) as Promise<BaseResponse<boolean>>;
}

/**
 * 删除科室
 * @param id 科室ID
 */
export function deleteDepartment(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/department/delete/${id}`) as Promise<BaseResponse<boolean>>;
}
