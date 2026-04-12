import request from '../request';
import { BaseResponse } from '../types';
import { API_BASE_URL } from '../../config/service';

// 文件上传业务类型
export type FileBizType = 
  | 'user_avatar' 
  | 'companion_avatar' 
  | 'hospital_image' 
  | 'id_card' 
  | 'qualification' 
  | 'medical_record' 
  | 'order_attachment';

// 文件上传响应
export interface FileUploadResponse {
  url: string;
  filename: string;
  size: number;
}

export interface MinioObjectItem {
  name: string;
  objectPath: string;
  directory: boolean;
  size: number;
  lastModified?: string;
}

/**
 * 上传文件
 * @param file 文件对象
 * @param biz 业务类型，默认 user_avatar
 */
export function uploadFile(file: File, biz: FileBizType = 'user_avatar'): Promise<BaseResponse<string>> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('biz', biz);
  
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 批量上传文件
 * @param files 文件数组
 * @param biz 业务类型
 */
export function uploadFiles(files: File[], biz: FileBizType = 'user_avatar'): Promise<BaseResponse<string[]>> {
  const promises = files.map(file => uploadFile(file, biz));
  return Promise.all(promises).then(results => {
    return {
      code: 0,
      data: results.map(r => r.data),
      message: 'success',
    };
  });
}

/**
 * 获取文件下载 URL
 * @param biz 业务类型
 * @param userId 用户ID
 * @param filename 文件名
 */
export function getFileDownloadUrl(biz: string, userId: number, filename: string): string {
  return `${API_BASE_URL}/file/download/${biz}/${userId}/${filename}`;
}

/**
 * 获取文件预览 URL
 * @param biz 业务类型
 * @param userId 用户ID
 * @param filename 文件名
 */
export function getFilePreviewUrl(biz: string, userId: number, filename: string): string {
  return `${API_BASE_URL}/file/preview/${biz}/${userId}/${filename}`;
}

/**
 * 下载文件
 * @param biz 业务类型
 * @param userId 用户ID
 * @param filename 文件名
 */
export function downloadFile(biz: string, userId: number, filename: string): void {
  const url = getFileDownloadUrl(biz, userId, filename);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

/**
 * 管理端：列出 MinIO 目录
 */
export function listMinioObjects(prefix?: string): Promise<BaseResponse<MinioObjectItem[]>> {
  return request.get('/file/admin/list', {
    params: { prefix: prefix || undefined },
  }) as Promise<BaseResponse<MinioObjectItem[]>>;
}

/**
 * 管理端：创建文件夹
 */
export function createMinioFolder(prefix: string, folderName: string): Promise<BaseResponse<string>> {
  return request.post('/file/admin/folder', null, {
    params: { prefix: prefix || undefined, folderName },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 管理端：上传文件到指定目录
 */
export function uploadMinioFile(file: File, prefix?: string): Promise<BaseResponse<string>> {
  const formData = new FormData();
  formData.append('file', file);
  if (prefix) {
    formData.append('prefix', prefix);
  }
  return request.post('/file/admin/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 管理端：删除对象（文件/目录）
 */
export function deleteMinioObject(objectPath: string, directory = false): Promise<BaseResponse<boolean>> {
  return request.post('/file/admin/delete', null, {
    params: { objectPath, directory },
  }) as Promise<BaseResponse<boolean>>;
}

/**
 * 管理端：对象预览地址
 */
export function getMinioPreviewUrl(objectPath: string): string {
  return `${API_BASE_URL}/file/admin/preview?objectPath=${encodeURIComponent(objectPath)}`;
}

/**
 * 管理端：对象下载地址
 */
export function getMinioDownloadUrl(objectPath: string): string {
  return `${API_BASE_URL}/file/admin/download?objectPath=${encodeURIComponent(objectPath)}`;
}
