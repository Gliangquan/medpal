import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

// 医生信息
export interface Doctor {
  id: number;
  hospitalId: number;
  departmentId: number;
  doctorName: string;
  title?: string; // 职称：主任医师、副主任医师 等
  specialty?: string; // 专长
  introduction?: string; // 简介
  photoUrl?: string; // 照片
  status: number; // 0-禁用，1-启用
  createTime: string;
  updateTime: string;
}

/**
 * 获取医生列表
 * @param current 当前页码
 * @param size 每页大小
 * @param departmentId 科室ID（可选）
 * @param hospitalId 医院ID（可选）
 */
export function listDoctors(
  current: number = 1,
  size: number = 10,
  departmentId?: number,
  hospitalId?: number,
  keyword?: string
): Promise<BaseResponse<PageData<Doctor>>> {
  return request.get('/doctor/list', {
    params: { current, size, departmentId, hospitalId, keyword },
  }) as Promise<BaseResponse<PageData<Doctor>>>;
}

/**
 * 获取医生详情
 * @param id 医生ID
 */
export function getDoctor(id: number): Promise<BaseResponse<Doctor>> {
  return request.get(`/doctor/${id}`) as Promise<BaseResponse<Doctor>>;
}

/**
 * 创建医生
 * @param doctor 医生信息
 */
export function createDoctor(doctor: Partial<Doctor>): Promise<BaseResponse<Doctor>> {
  return request.post('/doctor/create', doctor) as Promise<BaseResponse<Doctor>>;
}

/**
 * 更新医生信息
 * @param doctor 医生信息
 */
export function updateDoctor(doctor: Partial<Doctor> & { id: number }): Promise<BaseResponse<boolean>> {
  return request.post('/doctor/update', doctor) as Promise<BaseResponse<boolean>>;
}

/**
 * 删除医生
 * @param id 医生ID
 */
export function deleteDoctor(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/doctor/delete/${id}`) as Promise<BaseResponse<boolean>>;
}
