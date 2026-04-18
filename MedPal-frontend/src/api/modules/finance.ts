import request from '../request';
import { BaseResponse } from '../types';

export interface FinanceSummary {
  totalIncome: number;
  pendingAmount: number;
  settledAmount: number;
  platformFee: number;
}

export interface FinanceSettlementRow {
  id: number;
  companionId: number;
  companionName: string;
  phone: string;
  orderCount: number;
  totalHours: number;
  amount: number;
  fee: number;
  actualAmount: number;
  status: string;
  settlementTime?: string;
  settlementNo?: string;
}

export interface FinanceSettlementPage {
  records: FinanceSettlementRow[];
  total: number;
  current: number;
  size: number;
}

export function getFinanceSummary(): Promise<BaseResponse<FinanceSummary>> {
  return request.get('/finance/summary') as Promise<BaseResponse<FinanceSummary>>;
}

export function listFinanceSettlements(current = 1, size = 10, status?: string, keyword?: string): Promise<BaseResponse<FinanceSettlementPage>> {
  return request.get('/finance/settlement/list', {
    params: { current, size, status, keyword }
  }) as Promise<BaseResponse<FinanceSettlementPage>>;
}

export function processFinanceSettlement(id: number): Promise<BaseResponse<string>> {
  return request.post(`/finance/settlement/${id}`) as Promise<BaseResponse<string>>;
}

export function batchProcessFinanceSettlements(ids: string): Promise<BaseResponse<string>> {
  return request.post('/finance/settlement/batch', null, {
    params: { ids }
  }) as Promise<BaseResponse<string>>;
}
