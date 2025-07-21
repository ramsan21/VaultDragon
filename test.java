- name: Remove run.sh and rename ctrun.sh to run.sh if ENV is STG or SIT3
  when: ENV in ['STG', 'SIT3']
  block:
    - name: Remove existing run.sh
      file:
        path: "{{ temp_dir }}/run.sh"
        state: absent

    - name: Rename ctrun.sh to run.sh if it exists
      command: mv ctrun.sh run.sh
      args:
        chdir: "{{ temp_dir }}"
      when: 
        - lookup('ansible.builtin.file', temp_dir + '/ctrun.sh') is not none
        - lookup('ansible.builtin.file', temp_dir + '/ctrun.sh') != ''
        - ansible.builtin.stat(path=temp_dir + '/ctrun.sh').stat.exists