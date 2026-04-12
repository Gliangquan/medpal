// 兼容层：将旧的类式 API 转换为新的函数式 API
// 这个文件用于兼容仍然使用旧 API 导入方式的组件

import * as userApi from './modules/user';
import * as fileApi from './modules/file';
import * as hospitalApi from './modules/hospital';
import * as orderApi from './modules/order';
import * as paymentApi from './modules/payment';

// UserControllerApi 兼容类
export class UserControllerApi {
  async userLoginUsingPOST(data: any) {
    return userApi.userLogin(data);
  }
  
  async userRegisterUsingPOST(data: any) {
    return userApi.userRegister(data);
  }
  
  async userLogoutUsingPOST() {
    return userApi.userLogout();
  }
  
  async getLoginUserUsingGET() {
    return userApi.getLoginUser();
  }
  
  async addUserUsingPOST(data: any) {
    return userApi.addUser(data);
  }
  
  async deleteUserUsingPOST(data: any) {
    return userApi.deleteUser(data);
  }
  
  async updateUserUsingPOST(data: any) {
    return userApi.updateUser(data);
  }
  
  async getUserByIdUsingGET(params: any) {
    return userApi.getUserById(params.id);
  }
  
  async getUserVOByIdUsingGET(params: any) {
    return userApi.getUserVOById(params.id);
  }
  
  async listUserByPageUsingPOST(data: any) {
    return userApi.listUserByPage(data);
  }
  
  async listUserVOByPageUsingPOST(data: any) {
    return userApi.listUserVOByPage(data);
  }
  
  async updateMyUserUsingPOST(data: any) {
    return userApi.updateMyUser(data);
  }
  
  async batchDeleteUserUsingPOST(data: any) {
    return userApi.batchDeleteUser(data);
  }
  
  async batchUpdateUserUsingPOST(data: any) {
    return userApi.batchUpdateUser(data);
  }
  
  async getUserStatisticsUsingGET() {
    return userApi.getUserStatistics();
  }
}

// FileControllerApi 兼容类
export class FileControllerApi {
  async uploadFileUsingPOST(file: File, biz?: string) {
    return fileApi.uploadFile(file, biz as any);
  }
  
  getFileDownloadUrl(biz: string, userId: number, filename: string) {
    return fileApi.getFileDownloadUrl(biz, userId, filename);
  }
  
  getFilePreviewUrl(biz: string, userId: number, filename: string) {
    return fileApi.getFilePreviewUrl(biz, userId, filename);
  }
}

// 占位符类（尚未实现后端 Controller 的 API）
// 这些 API 在后端不存在，保留兼容层以便未来实现

export class ClassesControllerApi {
  constructor() {
    console.warn('ClassesControllerApi 尚未实现');
  }
}

export class ClassChatControllerApi {
  constructor() {
    console.warn('ClassChatControllerApi 尚未实现');
  }
}

export class NewsControllerApi {
  constructor() {
    console.warn('NewsControllerApi 尚未实现');
  }
}

export class QuestionControllerApi {
  constructor() {
    console.warn('QuestionControllerApi 尚未实现');
  }
}

export class QuestionAssignmentControllerApi {
  constructor() {
    console.warn('QuestionAssignmentControllerApi 尚未实现');
  }
}

export class QuestionSubmissionControllerApi {
  constructor() {
    console.warn('QuestionSubmissionControllerApi 尚未实现');
  }
}

export class CommentsControllerApi {
  constructor() {
    console.warn('CommentsControllerApi 尚未实现');
  }
}

export class VideoControllerApi {
  constructor() {
    console.warn('VideoControllerApi 尚未实现');
  }
}

// 导出类型占位符
export class DeleteRequest {}
export class UserQueryRequest {}
export class UserAddRequest {}
export class UserUpdateRequest {}
export class UserLoginRequest {
  loginType?: 'phone' | 'account';
  userPhone?: string;
  userAccount?: string;
  userPassword?: string;
}
export class UserRegisterRequest {
  userAccount?: string;
  userPassword?: string;
  checkPassword?: string;
  userPhone?: string;
}
export class NewsQueryRequest {}
export class NewsAddRequest {}
export class NewsUpdateRequest {}
export class QuestionQueryRequest {}
export class QuestionAddRequest {}
export class QuestionUpdateRequest {}
