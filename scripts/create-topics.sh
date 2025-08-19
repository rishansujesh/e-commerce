#!/usr/bin/env bash
set -euo pipefail
BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}
TOPICS=(
  order.created
  order.confirmed
  order.cancelled
  payment.authorized
  payment.captured
  payment.failed
  shipment.scheduled
  shipment.dispatched
)
for topic in "${TOPICS[@]}"; do
  kafka-topics --bootstrap-server "$BOOTSTRAP_SERVERS" --create --if-not-exists --topic "$topic" --replication-factor 1 --partitions 3 || true
done
