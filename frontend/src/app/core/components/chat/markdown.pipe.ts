import { Pipe, PipeTransform } from '@angular/core';

/**
 * Convierte un subconjunto seguro de Markdown (el que puede devolver el
 * asistente) a HTML. Escapa primero el texto para evitar XSS, por lo que el
 * resultado solo contiene etiquetas controladas.
 */
@Pipe({ name: 'markdown', standalone: true })
export class MarkdownPipe implements PipeTransform {
  transform(value: string): string {
    if (!value) {
      return '';
    }

    const lines = value.split('\n');
    const html: string[] = [];
    let i = 0;

    while (i < lines.length) {
      const line = lines[i];
      const trimmed = line.trim();

      // Tabla (bloque de lineas que empiezan con "|")
      if (trimmed.startsWith('|')) {
        const tableLines: string[] = [];
        while (i < lines.length && lines[i].trim().startsWith('|')) {
          tableLines.push(lines[i].trim());
          i++;
        }
        html.push(this.renderTable(tableLines));
        continue;
      }

      // Lista (viñetas u ordenada)
      const isOrdered = /^\d+[.)]\s+/.test(trimmed);
      const isUnordered = /^[-*]\s+/.test(trimmed);
      if (isOrdered || isUnordered) {
        const items: string[] = [];
        while (i < lines.length) {
          const m = isOrdered
            ? lines[i].trim().match(/^\d+[.)]\s+(.*)$/)
            : lines[i].trim().match(/^[-*]\s+(.*)$/);
          if (!m) {
            break;
          }
          items.push(`<li>${this.renderInline(this.escapeHtml(m[1]))}</li>`);
          i++;
        }
        html.push(isOrdered ? `<ol>${items.join('')}</ol>` : `<ul>${items.join('')}</ul>`);
        continue;
      }

      // Linea en blanco
      if (trimmed === '') {
        html.push('');
        i++;
        continue;
      }

      // Linea normal
      html.push(this.renderInline(this.escapeHtml(line)));
      i++;
    }

    return html.join('<br>');
  }

  private renderTable(lines: string[]): string {
    const rows = lines.filter((l) => !/^\|[\s:|-]+\|$/.test(l));
    if (rows.length === 0) {
      return '';
    }

    const parse = (l: string): string[] =>
      l
        .replace(/^\|/, '')
        .replace(/\|$/, '')
        .split('|')
        .map((c) => c.trim());

    const header = parse(rows[0]);
    const bodyRows = rows.slice(1);

    const thead = `<thead><tr>${header
      .map((c) => `<th>${this.renderInline(this.escapeHtml(c))}</th>`)
      .join('')}</tr></thead>`;

    const tbody = bodyRows.length
      ? `<tbody>${bodyRows
          .map(
            (r) =>
              `<tr>${parse(r)
                .map((c) => `<td>${this.renderInline(this.escapeHtml(c))}</td>`)
                .join('')}</tr>`,
          )
          .join('')}</tbody>`
      : '';

    return `<table>${thead}${tbody}</table>`;
  }

  private renderInline(text: string): string {
    return text
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\*([^*]+)\*/g, '<em>$1</em>');
  }

  private escapeHtml(text: string): string {
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
}
