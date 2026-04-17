import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

// 医院信息
export interface Hospital {
  id: number;
  hospitalName: string;
  hospitalCode?: string;
  address?: string;
  phone?: string;
  hospitalLevel?: string;
  level?: string; // 兼容旧字段
  type?: string; // 医院类型：综合医院、专科医院 等
  introduction?: string;
  description?: string; // 兼容旧字段
  logoUrl?: string;
  status: number; // 0-禁用，1-启用
  createTime: string;
  updateTime: string;
}

// 附近医院 VO（包含距离）
export interface HospitalNearbyVO extends Hospital {
  distance: number; // 距离（公里）
}

// 医院搜索请求
export interface HospitalSearchRequest {
  keyword: string;
}

/**
 * 获取医院列表
 * @param current 当前页码
 * @param size 每页大小
 */
export function listHospitals(current: number = 1, size: number = 10): Promise<BaseResponse<PageData<Hospital>>> {
  return request.get('/hospital/list', { params: { current, size } }) as Promise<BaseResponse<PageData<Hospital>>>;
}

/**
 * 获取医院详情
 * @param id 医院ID
 */
export function getHospital(id: number): Promise<BaseResponse<Hospital>> {
  return request.get(`/hospital/${id}`) as Promise<BaseResponse<Hospital>>;
}

/**
 * 搜索医院
 * @param keyword 搜索关键词
 */
export function searchHospitals(keyword: string): Promise<BaseResponse<Hospital[]>> {
  return request.post('/hospital/search', { keyword }) as Promise<BaseResponse<Hospital[]>>;
}

/**
 * 获取附近医院
 * @param latitude 纬度
 * @param longitude 经度
 * @param radius 搜索半径（公里，默认5）
 * @param limit 返回数量（默认20）
 */
export function getNearbyHospitals(
  latitude: number,
  longitude: number,
  radius: number = 5,
  limit: number = 20
): Promise<BaseResponse<HospitalNearbyVO[]>> {
  return request.get('/hospital/nearby', {
    params: { latitude, longitude, radius, limit },
  }) as Promise<BaseResponse<HospitalNearbyVO[]>>;
}

/**
 * 创建医院（管理员）
 * @param hospital 医院信息
 */
export function createHospital(hospital: Partial<Hospital>): Promise<BaseResponse<Hospital>> {
  return request.post('/hospital/create', hospital) as Promise<BaseResponse<Hospital>>;
}

/**
 * 更新医院信息
 * @param hospital 医院信息
 */
export function updateHospital(hospital: Partial<Hospital> & { id: number }): Promise<BaseResponse<boolean>> {
  return request.post('/hospital/update', hospital) as Promise<BaseResponse<boolean>>;
}

/**
 * 删除医院
 * @param id 医院ID
 */
export function deleteHospital(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/hospital/delete/${id}`) as Promise<BaseResponse<boolean>>;
}
