import numpy as np
import pandas as pd
import argparse
import json
from datetime import datetime
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense, Dropout
from sklearn.metrics import mean_absolute_error, mean_squared_error
from keras.callbacks import EarlyStopping
from scipy.optimize import minimize
import traceback
import sys

def parse_quarter(quarter_str):
    quarter, year = quarter_str.strip().split()
    month = {'Q1': 1, 'Q2': 4, 'Q3': 7, 'Q4': 10}[quarter]
    return datetime(int(year), month, 1)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--csv', type=str, required=True)
    parser.add_argument('--train-start', type=int, required=True)
    parser.add_argument('--train-end', type=int, required=True)
    parser.add_argument('--forecast-steps', type=int, required=True)
    args = parser.parse_args()

    df = pd.read_csv(args.csv)
    df['Период'] = df['Период'].apply(parse_quarter)
    df['Year'] = df['Период'].dt.year
    df['Quarter'] = df['Период'].dt.quarter

    df = df[(df['Year'] >= args.train_start) & (df['Year'] <= args.train_end)].copy()

    df['Year_norm'] = (df['Year'] - df['Year'].min()) / (df['Year'].max() - df['Year'].min())
    df['Quarter_norm'] = (df['Quarter'] - 1) / 3

    features = df[['Выручка (млрд)', 'Year_norm', 'Quarter_norm']].values
    scaler = MinMaxScaler()
    features_scaled = scaler.fit_transform(features)

    n_steps = 8
    n_outputs = args.forecast_steps

    X, y = [], []
    for i in range(len(features_scaled) - n_steps - n_outputs + 1):
        X.append(features_scaled[i:i+n_steps])
        y.append([features_scaled[i+n_steps + j][0] for j in range(n_outputs)])
    X, y = np.array(X), np.array(y)

    model = Sequential()
    model.add(LSTM(64, return_sequences=True, input_shape=(n_steps, X.shape[2])))
    model.add(Dropout(0.3))
    model.add(LSTM(32))
    model.add(Dense(n_outputs))
    model.compile(optimizer='adam', loss='mse')
    early_stop = EarlyStopping(patience=20, restore_best_weights=True)
    model.fit(X, y, epochs=200, verbose=0, callbacks=[early_stop])

    X_input = features_scaled[-n_steps:]
    X_input = np.expand_dims(X_input, axis=0)
    pred_scaled = model.predict(X_input)

    future_years = np.array([args.train_end + i // 4 for i in range(n_outputs)])
    future_quarters = np.array([((i % 4) + 1) for i in range(n_outputs)])
    year_norm = (future_years - df['Year'].min()) / (df['Year'].max() - df['Year'].min())
    quarter_norm = (future_quarters - 1) / 3
    inverse_features = np.stack([pred_scaled[0], year_norm, quarter_norm], axis=1)
    base_pred = scaler.inverse_transform(inverse_features)[:, 0]

    df_train = df[df['Year'] < args.train_end]
    season_avg_train = df_train.groupby('Quarter')['Выручка (млрд)'].mean().sort_index().values
    season_pred = np.tile(season_avg_train, int(np.ceil(n_outputs / 4)))[:n_outputs].reshape(-1, 1)

    df_val = df[df['Year'] == args.train_end]
    if len(df_val) >= n_outputs:
        real_val = df_val.sort_values('Quarter')['Выручка (млрд)'].values[:n_outputs]
        base_val_pred = model.predict(np.expand_dims(features_scaled[-n_steps:], axis=0))
        base_val = scaler.inverse_transform(np.concatenate([
            base_val_pred[0].reshape(-1, 1),
            np.tile([features_scaled[-1, 1], features_scaled[-1, 2]], (n_outputs, 1))
        ], axis=1))[:, 0]

        def loss_fn(alphas):
            combined = base_val * 0.85 + alphas * season_pred.flatten() * 0.15
            return mean_squared_error(real_val, combined)

        result = minimize(loss_fn, np.ones(n_outputs), bounds=[(0.5, 2.0)] * n_outputs)
        optimized_alphas = result.x
    else:
        optimized_alphas = np.ones(n_outputs)

    final_pred = base_pred * 0.85 + optimized_alphas * season_pred.flatten() * 0.15

    output = {
        'base_forecast': base_pred.tolist(),
        'final_forecast': final_pred.tolist(),
        'quarters': [f"Q{(i % 4) + 1} {future_years[i]}" for i in range(n_outputs)]
    }

    print(json.dumps(output, ensure_ascii=False))

if __name__ == '__main__':
    try:
        main()
    except Exception:
        traceback.print_exc(file=sys.stdout)
        sys.exit(1)
