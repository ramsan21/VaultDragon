- name: Find old temp directories to delete
  find:
    paths: "/prd/starss/tmp/"
    patterns: "{{ PKG_NAME }}-*"
    file_type: directory
  register: found_dirs

- name: Delete all old temp folders except current version
  file:
    path: "{{ item.path }}"
    state: absent
  loop: "{{ found_dirs.files }}"
  when: "'{{ PKG_NAME }}-{{ PKG_VERSION }}' not in item.path"