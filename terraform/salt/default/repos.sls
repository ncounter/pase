{% if grains['cpuarch'] == 'aarch64' %}

os_pool_repo:
  pkgrepo.managed:
    - baseurl: http://download.opensuse.org/ports/aarch64/distribution/leap/{{ grains['osrelease'] }}/repo/oss/

{% else %}

os_pool_repo:
  pkgrepo.managed:
    - baseurl: http://download.opensuse.org/distribution/leap/{{ grains['osrelease'] }}/repo/oss/

os_update_repo:
  pkgrepo.managed:
    - baseurl: http://download.opensuse.org/update/leap/{{ grains['osrelease'] }}/oss/

{% endif %}

refresh_repos:
  cmd.run:
    - name: zypper --non-interactive --gpg-auto-import-keys refresh
