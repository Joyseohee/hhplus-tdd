package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
	private val userPointTable: UserPointTable,
	private val pointHistoryTable: PointHistoryTable
) {
	// 유효한 사용자에 대한 요청인지 검증
	fun getUserPoint(userId: Long): UserPoint {
		return userPointTable.selectById(userId) ?: throw UserNotFoundException(userId)
	}

	fun getUserPointHistory(userId: Long): List<PointHistory> {
		getUserPoint(userId)

		// 포인트 내역을 시간 순으로 정렬
		return pointHistoryTable.selectAllByUserId(userId).sortedBy { it.timeMillis }
	}

	fun chargePoint(userId: Long, point: Long): UserPoint {
		val original = getUserPoint(userId)
		val charged = original.charge(point)

		try {
			val updated = userPointTable.insertOrUpdate(userId, charged.point)

			pointHistoryTable.insert(
				id = userId,
				amount = point,
				transactionType = TransactionType.CHARGE,
			)

			return updated
		} catch (e: Exception) {
			userPointTable.insertOrUpdate(userId, original.point)
			throw PointTransactionFailedException()
		}
	}

	fun usePoint(userId: Long, point: Long): UserPoint {
		val userPoint = getUserPoint(userId)

		val usedUserPoint = userPoint.use(point)

		try {
			val updatedUserPoint = userPointTable.insertOrUpdate(userId, usedUserPoint.point)

			pointHistoryTable.insert(
				id = userId,
				amount = point,
				transactionType = TransactionType.USE
			)

			return updatedUserPoint
		} catch (e: Exception) {
			userPointTable.insertOrUpdate(userId, userPoint.point)
			throw PointTransactionFailedException()
		}
	}
}

sealed class PointServiceException(message: String, ) : RuntimeException(message)

class UserNotFoundException(userId: Long) : PointServiceException("${userId}는 존재하지 않는 사용자입니다.")
class PointTransactionFailedException : PointServiceException("정상적으로 처리되지 않았습니다.")