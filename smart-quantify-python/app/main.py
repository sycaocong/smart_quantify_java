import grpc
from concurrent import futures
import time
import logging

import strategy_pb2
import strategy_pb2_grpc

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class StrategyServicer(strategy_pb2_grpc.StrategyServiceServicer):
    def Execute(self, request, context):
        logger.info(f"Executing strategy: {request.strategy_id}")
        
        kline = request.kline
        close = kline.close
        high = kline.high
        low = kline.low
        
        signal = strategy_pb2.SignalData()
        signal.id = "signal-" + str(time.time())
        signal.strategy_id = request.strategy_id
        signal.symbol = kline.symbol
        signal.exchange = "BINANCE"
        signal.created_at = str(time.time())
        signal.status = "PENDING"
        signal.priority = 1
        
        if close > (high + low) / 2:
            signal.side = "BUY"
            signal.type = "ENTRY"
            signal.price = close
            signal.quantity = 0.01
        elif close < (high + low) / 2:
            signal.side = "SELL"
            signal.type = "EXIT"
            signal.price = close
            signal.quantity = 0.01
        
        return strategy_pb2.ExecuteResponse(signal=signal)

    def Backtest(self, request, context):
        logger.info(f"Running backtest: {request.strategy_id}")
        
        initial_capital = request.initial_capital
        capital = initial_capital
        position = 0
        entry_price = 0
        max_capital = capital
        min_capital = capital
        
        winning_trades = 0
        losing_trades = 0
        
        for kline in request.klines:
            close = kline.close
            high = kline.high
            low = kline.low
            
            if position == 0:
                if close > (high + low) / 2:
                    position = (capital * 0.1) / close
                    entry_price = close
                    capital -= position * close
            else:
                profit = position * (close - entry_price)
                if profit > entry_price * position * 0.02:
                    capital += position * close
                    winning_trades += 1
                    position = 0
                elif profit < -entry_price * position * 0.01:
                    capital += position * close
                    losing_trades += 1
                    position = 0
            
            if capital > max_capital:
                max_capital = capital
            if capital < min_capital:
                min_capital = capital
        
        if position > 0:
            capital += position * request.klines[-1].close
        
        total_trades = winning_trades + losing_trades
        total_return = (capital - initial_capital) / initial_capital
        max_drawdown = (max_capital - min_capital) / max_capital
        win_rate = winning_trades / total_trades if total_trades > 0 else 0
        
        return strategy_pb2.BacktestResponse(
            task_id="backtest-" + str(time.time()),
            initial_capital=initial_capital,
            final_capital=capital,
            total_return=total_return,
            max_drawdown=max_drawdown,
            win_rate=win_rate,
            total_trades=total_trades,
            winning_trades=winning_trades,
            losing_trades=losing_trades
        )

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    strategy_pb2_grpc.add_StrategyServiceServicer_to_server(StrategyServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    logger.info("gRPC server started on port 50051")
    
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()