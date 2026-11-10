#!/usr/bin/env python3
"""Lee mutations.xml de PIT y genera: resumen en Markdown, tabla de supervivientes y badge JSON.

Uso:
  python3 scripts/mutation_report.py target/pit-reports/mutations.xml
  python3 scripts/mutation_report.py target/pit-reports/mutations.xml --badge mutation.json
"""
import argparse
import json
import sys
import xml.etree.ElementTree as ET
from collections import Counter

DETECTED = {"KILLED", "TIMED_OUT", "MEMORY_ERROR"}


def load(path):
    return list(ET.parse(path).getroot().iter("mutation"))


def score(mutations):
    total = len(mutations)
    detected = sum(1 for m in mutations if m.get("status") in DETECTED)
    return (100.0 * detected / total) if total else 0.0


def markdown(mutations):
    status = Counter(m.get("status") for m in mutations)
    lines = [
        "## Mutation Testing (PIT)",
        "",
        f"**Mutation Score: {score(mutations):.1f} %** ({len(mutations)} mutantes)",
        "",
        "| Estado | Cantidad |",
        "|---|---|",
    ]
    lines += [f"| {name} | {count} |" for name, count in sorted(status.items())]

    survivors = [m for m in mutations if m.get("status") in ("SURVIVED", "NO_COVERAGE")]
    if survivors:
        lines += ["", "### Mutantes que sobreviven (a analizar)", "",
                  "| Clase | Metodo | Linea | Mutador | Estado |", "|---|---|---|---|---|"]
        for m in survivors:
            cls = (m.findtext("mutatedClass") or "").split(".")[-1]
            mutator = (m.findtext("mutator") or "").split(".")[-1]
            lines.append(f"| {cls} | {m.findtext('mutatedMethod')} | {m.findtext('lineNumber')} "
                         f"| {mutator} | {m.get('status')} |")
    return "\n".join(lines)


def badge(value):
    color = "brightgreen" if value >= 60 else "yellow" if value >= 50 else "red"
    return {"schemaVersion": 1, "label": "mutation score", "message": f"{value:.0f}%", "color": color}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("mutations_xml")
    parser.add_argument("--badge", help="ruta del JSON del badge (shields.io endpoint)")
    args = parser.parse_args()

    mutations = load(args.mutations_xml)
    if args.badge:
        with open(args.badge, "w", encoding="utf-8") as f:
            json.dump(badge(score(mutations)), f)
    else:
        print(markdown(mutations))


if __name__ == "__main__":
    sys.exit(main())
