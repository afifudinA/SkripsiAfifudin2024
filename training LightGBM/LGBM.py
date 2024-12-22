import numpy as np
import pandas as pd
import lightgbm as lgb
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from sklearn.model_selection import train_test_split
import joblib
import os
os.environ["LOKY_MAX_CPU_COUNT"] = "4"

# Load training and test datasets
train_file_path = 'train_data.csv'
# test_file_path = 'C:\\Users\\cern\\Desktop\\s3\\done\\ml data\\new_test.csv'
train_data = pd.read_csv(train_file_path)
# test_data = pd.read_csv(test_file_path)

# # Define features and target
# X_train = train_data[['sin_hour', 'hour', 'stopOrder', 'nextStopOrder']]
# y_train = train_data['time_diff']
# X_test = test_data[['sin_hour', 'hour', 'stopOrder', 'nextStopOrder']]
# y_test = test_data['time_diff']

X = train_data[['sin_hour', 'stopOrder', 'nextStopOrder']]
y = train_data['time_diff']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42)

# 初始化模型并设置初始参数
lgb_model = lgb.LGBMRegressor(
    n_estimators=100,
    max_depth=30,
    num_leaves=31,
    min_child_samples=30,
    subsample=0.8,
    colsample_bytree=1.0,
    learning_rate=0.05,
)


lgb_model.fit(
    X_train, y_train,
    eval_set=[(X_test, y_test)],
    eval_metric='rmse'
)

# X_test = test_data[['sin_hour', 'hour', 'stopOrder', 'nextStopOrder']]
# y_test = test_data['time_diff']


y_pred = lgb_model.predict(X_train)


mse = mean_squared_error(y_train, y_pred)
rmse = np.sqrt(mse)
mae = mean_absolute_error(y_train, y_pred)
r2 = r2_score(y_train, y_pred)

# model_file_path = 'C:\\Users\\cern\\Desktop\\s3\\lgb_model.pkl'
# joblib.dump(lgb_model, model_file_path)

print(f"MSE:{mse}, RMSE: {rmse}, MAE: {mae}, R²: {r2}")
