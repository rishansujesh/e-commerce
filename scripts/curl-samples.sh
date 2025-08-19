#!/usr/bin/env bash
set -euo pipefail

CATALOG=http://localhost:8081
ORDERS=http://localhost:8082

# create products
echo "Creating products..."
curl -s -X POST "$CATALOG/v1/products" -H 'Content-Type: application/json' -d '{"sku":"sku1","name":"Item 1","price":10.00,"stock":100}'

echo
echo "Creating second product..."
curl -s -X POST "$CATALOG/v1/products" -H 'Content-Type: application/json' -d '{"sku":"sku2","name":"Item 2","price":20.00,"stock":50}'

# place order
echo
echo "Placing order..."
ORDER_ID=$(curl -s -X POST "$ORDERS/v1/orders" -H 'Content-Type: application/json' -d '{"items":[{"sku":"sku1","qty":1},{"sku":"sku2","qty":2}]}' | jq -r '.id')
echo "Order ID: $ORDER_ID"

# poll order status
echo "Polling order status..."
curl -s "$ORDERS/v1/orders/$ORDER_ID" | jq '.'

# cancel order
echo
echo "Cancelling order..."
curl -s -X POST "$ORDERS/v1/orders/$ORDER_ID/cancel" | jq '.'
