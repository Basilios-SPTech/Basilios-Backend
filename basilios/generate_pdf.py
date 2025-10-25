#!/usr/bin/env python3
# Gera um PDF simples a partir de README_OBSERVER.md

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Preformatted, Spacer
from reportlab.lib.units import cm
import os
import sys

INPUT_MD = 'README_OBSERVER.md'
OUTPUT_PDF = 'Observer_PI.pdf'

if not os.path.exists(INPUT_MD):
    print(f'Arquivo {INPUT_MD} não encontrado. Execute este script na raiz do projeto onde o arquivo existe.')
    sys.exit(2)

with open(INPUT_MD, 'r', encoding='utf-8') as f:
    md = f.read()

# Cria documento
doc = SimpleDocTemplate(OUTPUT_PDF, pagesize=A4,
                        rightMargin=2*cm, leftMargin=2*cm,
                        topMargin=2*cm, bottomMargin=2*cm)

mono_style = ParagraphStyle('mono')
mono_style.fontName = 'Courier'
mono_style.fontSize = 9
mono_style.leading = 11

# Usa Preformatted para manter formatação de código e bloqueios de markdown
story = []
story.append(Preformatted(md, mono_style))

try:
    doc.build(story)
    print(f'PDF gerado com sucesso: {OUTPUT_PDF}')
except Exception as e:
    print('Falha ao gerar o PDF:', e)
    sys.exit(1)

