(function() {
    'use strict';

    angular
        .module('backendApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('event-image-vote', {
            parent: 'entity',
            url: '/event-image-vote?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventImageVote.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-image-vote/event-image-votes.html',
                    controller: 'EventImageVoteController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventImageVote');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('event-image-vote-detail', {
            parent: 'entity',
            url: '/event-image-vote/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventImageVote.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-image-vote/event-image-vote-detail.html',
                    controller: 'EventImageVoteDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventImageVote');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'EventImageVote', function($stateParams, EventImageVote) {
                    return EventImageVote.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'event-image-vote',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('event-image-vote-detail.edit', {
            parent: 'event-image-vote-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-image-vote/event-image-vote-dialog.html',
                    controller: 'EventImageVoteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventImageVote', function(EventImageVote) {
                            return EventImageVote.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-image-vote.new', {
            parent: 'event-image-vote',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-image-vote/event-image-vote-dialog.html',
                    controller: 'EventImageVoteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                vote: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('event-image-vote', null, { reload: 'event-image-vote' });
                }, function() {
                    $state.go('event-image-vote');
                });
            }]
        })
        .state('event-image-vote.edit', {
            parent: 'event-image-vote',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-image-vote/event-image-vote-dialog.html',
                    controller: 'EventImageVoteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventImageVote', function(EventImageVote) {
                            return EventImageVote.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-image-vote', null, { reload: 'event-image-vote' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-image-vote.delete', {
            parent: 'event-image-vote',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-image-vote/event-image-vote-delete-dialog.html',
                    controller: 'EventImageVoteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EventImageVote', function(EventImageVote) {
                            return EventImageVote.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-image-vote', null, { reload: 'event-image-vote' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
